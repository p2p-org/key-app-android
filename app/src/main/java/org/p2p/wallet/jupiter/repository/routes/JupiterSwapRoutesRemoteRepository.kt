package org.p2p.wallet.jupiter.repository.routes

import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.utils.emptyString
import org.p2p.wallet.jupiter.api.SwapJupiterApi
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.model.SwapFailure
import org.p2p.wallet.jupiter.repository.tokens.db.SwapTokensDaoDelegate
import org.p2p.wallet.utils.retryOnException

private const val TAG = "JupiterSwapRoutesRemoteRepository"

class JupiterSwapRoutesRemoteRepository(
    private val api: SwapJupiterApi,
    private val dispatchers: CoroutineDispatchers,
    private val mapper: JupiterSwapRoutesMapper,
    private val routeValidator: JupiterSwapRouteValidator,
    private val daoDelegate: SwapTokensDaoDelegate,
) : JupiterSwapRoutesRepository {

    override suspend fun getSwapRoutesForSwapPair(
        jupiterSwapPair: JupiterSwapPair,
        userPublicKey: Base58String,
        validateRoutes: Boolean,
    ): List<JupiterSwapRoute> = withContext(dispatchers.io) {
        try {
            // SocketTimeoutException can occur even when there's no real problem with the network
            // It may happen if a socket becomes stale/dangling due unstable/changed network or by server side
            // Also sometimes InterruptedIOException happens instead of timeout error, a can of worms
            // there's an ancient issue about the same thing https://github.com/square/okhttp/issues/1037
            val response = retryOnException {
                api.getSwapRoutes(
                    inputMint = jupiterSwapPair.inputMint.base58Value,
                    outputMint = jupiterSwapPair.outputMint.base58Value,
                    amountInLamports = jupiterSwapPair.amountInLamports,
                    userPublicKey = userPublicKey.base58Value,
                    slippageBps = jupiterSwapPair.slippageBasePoints
                )
            }
            val routes = mapper.fromNetwork(response)
            if (validateRoutes) {
                routeValidator.validateRoutes(routes)
            } else {
                routes
            }
        } catch (e: HttpException) {
            val isTooSmallAmountError = try {
                val json = JSONObject(e.response()?.errorBody()?.string() ?: emptyString())
                json.getString("message").contains("The value \"NaN\" cannot be converted to a number")
            } catch (e: Throwable) {
                false
            }
            val isUnknownServerError = e.code() in 500..503

            throw when {
                isTooSmallAmountError -> SwapFailure.TooSmallInputAmount(e)
                isUnknownServerError -> SwapFailure.ServerUnknownError(e)
                else -> e
            }
        }
    }

    override suspend fun getSwappableTokens(sourceTokenMint: Base58String): List<JupiterSwapToken> {
        return withContext(dispatchers.computation) {
            daoDelegate.getSwappableTokens(sourceTokenMint)
        }
            .also { Timber.i("Getting swappable tokens ${it.size}") }
    }
}

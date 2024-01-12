package org.p2p.wallet.jupiter.repository.v6

import org.json.JSONObject
import retrofit2.HttpException
import java.math.BigInteger
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.utils.Constants
import org.p2p.core.utils.emptyString
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.jupiter.api.SwapJupiterV6Api
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRouteV6
import org.p2p.wallet.jupiter.repository.model.SwapFailure
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRouteValidator
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.utils.retryOnException
import org.p2p.wallet.utils.toPublicKey

class JupiterSwapRoutesRemoteV6Repository(
    private val apiV6: SwapJupiterV6Api,
    private val dispatchers: CoroutineDispatchers,
    private val mapper: JupiterSwapRoutesV6Mapper,
    private val validator: JupiterSwapRouteValidator,
    private val amountRepository: RpcAmountRepository,
    // bad, should be repository
    private val addressInteractor: TransactionAddressInteractor
) : JupiterSwapRoutesV6Repository {
    override suspend fun getSwapRoutesForSwapPair(
        jupiterSwapPair: JupiterSwapPair,
        userPublicKey: Base58String,
        shouldValidateRoute: Boolean
    ): JupiterSwapRouteV6? = withContext(dispatchers.io) {
        try {
            // SocketTimeoutException can occur even when there's no real problem with the network
            // It may happen if a socket becomes stale/dangling due unstable/changed network or by server side
            // Also sometimes InterruptedIOException happens instead of timeout error, a can of worms
            // there's an ancient issue about the same thing https://github.com/square/okhttp/issues/1037
            val response = retryOnException {
                apiV6.getSwapRoute(
                    inputMint = jupiterSwapPair.inputMint.base58Value,
                    outputMint = jupiterSwapPair.outputMint.base58Value,
                    amountInLamports = jupiterSwapPair.amountInLamports,
                    slippageBps = jupiterSwapPair.slippageBasePoints
                )
            }
            val tokenAccountFee = tokenAccountCreationFee(
                mintAddress = jupiterSwapPair.outputMint.base58Value,
                userPublicKey = userPublicKey.base58Value
            )
            val domainModel = mapper.fromNetwork(response, tokenAccountFee)

            if (shouldValidateRoute) {
                validator.validateRouteV6(domainModel)
            } else {
                domainModel
            }
        } catch (e: HttpException) {
            onRouteFetchFailed(e)
        }
    }

    private suspend fun tokenAccountCreationFee(mintAddress: String, userPublicKey: String): BigInteger {
        val shouldCreateAccount =
            mintAddress != Constants.WRAPPED_SOL_MINT && addressInteractor.findSplTokenAddressData(
                mintAddress = mintAddress,
                destinationAddress = userPublicKey.toPublicKey()
            ).shouldCreateAccount

        return if (shouldCreateAccount) {
            amountRepository.getMinBalanceForRentExemption(
                TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
            )
        } else {
            BigInteger.ZERO
        }
    }

    private fun onRouteFetchFailed(e: HttpException): Nothing {
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

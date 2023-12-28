package org.p2p.wallet.jupiter.repository.routes

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import okio.use
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.utils.emptyString
import org.p2p.wallet.common.date.toDateTimeString
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.common.storage.FilesDirStorageRepository
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.api.SwapJupiterApi
import org.p2p.wallet.jupiter.api.response.JupiterAllSwapRoutesResponse
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.SwapFailure
import org.p2p.wallet.utils.retryOnException

private const val TAG = "JupiterSwapRoutesRemoteRepository"

class JupiterSwapRoutesRemoteRepository(
    private val api: SwapJupiterApi,
    private val dispatchers: CoroutineDispatchers,
    private val mapper: JupiterSwapRoutesMapper,
    private val localRepository: JupiterSwapRoutesLocalRepository,
    private val swapStorage: JupiterSwapStorageContract,
    private val fileRepository: FilesDirStorageRepository,
    private val routeValidator: JupiterSwapRouteValidator,
    private val gson: Gson
) : JupiterSwapRoutesRepository {

    override suspend fun loadAndCacheAllSwapRoutes() {
        getSwapRoutes()
    }

    private suspend fun getSwapRoutes(): JupiterAvailableSwapRoutesMap = withContext(dispatchers.io) {
        getSwapRoutesFromCache() ?: fetchSwapRoutes().let {
            saveToStorage(it)
            getSwapRoutesFromCache()!!
        }
    }

    private suspend fun getSwapRoutesFromCache(): JupiterAvailableSwapRoutesMap? {
        if (!isCacheCanBeUsed()) {
            Timber.tag(TAG).i("Cannot use the cache for routes")
            return null
        }
        Timber.tag(TAG).i("Cache is valid, using cache")
        val localCache = localRepository.getCachedAllSwapRoutes()

        if (localCache != null) {
            return localCache
        }
        return fileRepository.readJsonFileAsStream("swap_routes.json")
            ?.bufferedReader()
            ?.let(::JsonReader)
            ?.let {
                val routes = gson.fromJson<JupiterAllSwapRoutesResponse>(it, JupiterAllSwapRoutesResponse::class.java)
                JupiterAvailableSwapRoutesMap(
                    tokenMints = routes.mintKeys,
                    allRoutes = routes.routeMap.mapKeys { it.key.toInt() }
                )
            }
    }

    private suspend fun fetchSwapRoutes(): InputStream {
        Timber.tag(TAG).i("Fetching new routes, cache is empty")
        return api.getSwapRoutesMap().byteStream()
    }

    private suspend fun saveToStorage(jsonBody: InputStream) {
        swapStorage.routesFetchDateMillis = System.currentTimeMillis()
        val result = jsonBody.use {
            fileRepository.saveAsJsonFile(it, "swap_routes.json")
        }

        val updateDate = swapStorage.routesFetchDateMillis?.toZonedDateTime()?.toDateTimeString()
        Timber.tag(TAG).i("Updated routes cache: date=$updateDate; routes_size=${result} ")
    }

    override suspend fun getSwapRoutesForSwapPair(
        jupiterSwapPair: JupiterSwapPair,
        userPublicKey: Base58String
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
            mapper.fromNetwork(response)
                .let { routeValidator.validateRoutes(it) }
        } catch (e: HttpException) {
            val isTooSmallAmountError = try {
                val json = JSONObject(e.response()?.errorBody()?.string() ?: emptyString())
                json.getString("message").contains("The value \"NaN\" cannot be converted to a number")
            } catch (e: Throwable) {
                false
            }
            throw if (isTooSmallAmountError) SwapFailure.TooSmallInputAmount(e) else e
        }
    }

    override suspend fun getSwappableTokenMints(sourceTokenMint: Base58String): List<Base58String> {
        return withContext(dispatchers.computation) {
            val allSwapRoutes = getSwapRoutes()

            val indexOfSourceToken: Int = allSwapRoutes.tokenMints.indexOfFirst { sourceTokenMint == it }
            val swappableTokensIndexes = allSwapRoutes.allRoutes[indexOfSourceToken].orEmpty()
            val swappableTokensMints: List<Base58String> = swappableTokensIndexes.mapNotNull {
                allSwapRoutes.tokenMints.getOrNull(it)
            }
            swappableTokensMints
        }
    }

    private fun isCacheCanBeUsed(): Boolean {
        val fetchRoutesDate = swapStorage.routesFetchDateMillis ?: return false
        val now = System.currentTimeMillis()
        return (now - fetchRoutesDate) <= TimeUnit.DAYS.toMillis(1) // check day has passed
    }
}

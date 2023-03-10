package org.p2p.wallet.jupiter.repository.routes

import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.withContext
import org.p2p.wallet.common.date.toDateTimeString
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.api.SwapJupiterApi
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.utils.Base58String

private const val TAG = "JupiterSwapRoutesRemoteRepository"

class JupiterSwapRoutesRemoteRepository(
    private val api: SwapJupiterApi,
    private val dispatchers: CoroutineDispatchers,
    private val mapper: JupiterSwapRoutesMapper,
    private val localRepository: JupiterSwapRoutesLocalRepository,
    private val swapStorage: JupiterSwapStorageContract
) : JupiterSwapRoutesRepository {

    override suspend fun loadAndCacheAllSwapRoutes() {
        getSwapRoutes()
    }

    private suspend fun getSwapRoutes(): JupiterAvailableSwapRoutesMap = withContext(dispatchers.io) {
        getSwapRoutesFromCache() ?: fetchSwapRoutes().also(::saveToStorage)
    }

    private fun getSwapRoutesFromCache(): JupiterAvailableSwapRoutesMap? {
        if (!isCacheCanBeUsed()) {
            Timber.tag(TAG).i("Cannot use the cache for routes")
            return null
        }
        Timber.tag(TAG).i("Cache is valid, using cache")
        return localRepository.getCachedAllSwapRoutes()
            ?: swapStorage.routesMap?.also(localRepository::setCachedSwapRoutes)
    }

    private suspend fun fetchSwapRoutes(): JupiterAvailableSwapRoutesMap {
        Timber.tag(TAG).i("Fetching new routes, cache is empty")
        return api.getSwapRoutesMap().let { response ->
            JupiterAvailableSwapRoutesMap(
                tokenMints = response.mintKeys,
                allRoutes = response.routeMap.mapKeys { it.key.toInt() }
            )
        }
    }

    private fun saveToStorage(routes: JupiterAvailableSwapRoutesMap) {
        swapStorage.routesFetchDateMillis = System.currentTimeMillis()
        swapStorage.routesMap = routes

        val updateDate = swapStorage.routesFetchDateMillis?.toZonedDateTime()?.toDateTimeString()
        Timber.tag(TAG).i("Updated routes cache: date=$updateDate; routes=${routes.allRoutes.size} ")
    }

    override suspend fun getSwapRoutesForSwapPair(
        jupiterSwapPair: JupiterSwapPair,
        userPublicKey: Base58String
    ): List<JupiterSwapRoute> = with(dispatchers.io) {
        val response = api.getSwapRoutes(
            inputMint = jupiterSwapPair.inputMint.base58Value,
            outputMint = jupiterSwapPair.outputMint.base58Value,
            amountInLamports = jupiterSwapPair.amountInLamports,
            userPublicKey = userPublicKey.base58Value,
            slippageBps = jupiterSwapPair.slippageBasePoints
        )
        mapper.fromNetwork(response)
    }

    override suspend fun getSwappableTokenMints(sourceTokenMint: Base58String): List<Base58String> {
        return withContext(dispatchers.computation) {
            val allSwapRoutes = getSwapRoutes()

            val indexOfSourceToken: Int = allSwapRoutes.tokenMints.indexOfFirst { sourceTokenMint == it }
            val swappableTokensIndexes: List<Int> = allSwapRoutes.allRoutes[indexOfSourceToken].orEmpty()
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

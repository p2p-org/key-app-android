package org.p2p.wallet.swap.jupiter.repository.routes

import kotlinx.coroutines.withContext
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterApi
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.utils.Base58String

class JupiterSwapRoutesRemoteRepository(
    private val api: SwapJupiterApi,
    private val dispatchers: CoroutineDispatchers,
    private val mapper: JupiterSwapRoutesMapper,
    private val localRepository: JupiterSwapRoutesLocalRepository
) : JupiterSwapRoutesRepository {

    override suspend fun loadAndCacheAllSwapRoutes() {
        localRepository.setCachedSwapRoutes(getAllSwapRoutes())
    }

    private suspend fun getAllSwapRoutes(): JupiterAvailableSwapRoutesMap {
        return api.getSwapRoutesMap().let { response ->
            JupiterAvailableSwapRoutesMap(
                tokenMints = response.mintKeys,
                allRoutes = response.routeMap.mapKeys { it.key.toInt() }
            )
        }
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
            val allSwapRoutes = localRepository.getCachedAllSwapRoutes()
                ?: getAllSwapRoutes()
                    .also(localRepository::setCachedSwapRoutes)

            val indexOfSourceToken: Int = allSwapRoutes.tokenMints.indexOfFirst { sourceTokenMint == it }
            val swappableTokensIndexes: List<Int> = allSwapRoutes.allRoutes[indexOfSourceToken].orEmpty()
            val swappableTokensMints: List<Base58String> = swappableTokensIndexes.mapNotNull {
                allSwapRoutes.tokenMints.getOrNull(it)
            }
            swappableTokensMints
        }
    }
}

package org.p2p.wallet.swap.jupiter.repository.routes

import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterApi
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwap
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.utils.Base58String

class JupiterSwapRoutesRemoteRepository(
    private val api: SwapJupiterApi,
    private val dispatchers: CoroutineDispatchers,
    private val mapper: JupiterSwapRoutesMapper,
    private val localRepository: JupiterSwapRoutesLocalRepository
) : JupiterSwapRoutesRepository {

    override suspend fun loadAllSwapRoutes() {
        val response = api.getSwapRoutesMap()
        localRepository.setCachedSwapRoutes(
            JupiterAvailableSwapRoutesMap(
                tokenMints = response.mintKeys,
                allRoutes = response.routeMap.mapKeys { it.key.toInt() }
            )
        )
    }

    override suspend fun getSwapRoutes(
        jupiterSwap: JupiterSwap,
        userPublicKey: Base58String
    ): List<JupiterSwapRoute> = with(dispatchers.io) {
        val response = api.getSwapRoutes(
            inputMint = jupiterSwap.inputMint,
            outputMint = jupiterSwap.outputMint,
            amountInLamports = jupiterSwap.amountInLamports,
            userPublicKey = userPublicKey
        )
        mapper.fromNetwork(response)
    }

    override suspend fun getSwappableTokenMints(sourceTokenMint: Base58String): List<Base58String> =
        localRepository.getSwappableTokenMints(sourceTokenMint)
}

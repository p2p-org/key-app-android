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
) : JupiterSwapRoutesRepository {

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
}

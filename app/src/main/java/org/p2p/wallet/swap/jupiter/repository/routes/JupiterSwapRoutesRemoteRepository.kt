package org.p2p.wallet.swap.jupiter.repository.routes

import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterApi
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwap
import org.p2p.wallet.swap.jupiter.repository.model.SwapRoute
import org.p2p.wallet.utils.Base58String

class JupiterSwapRoutesRemoteRepository(
    private val api: SwapJupiterApi,
    private val dispatchers: CoroutineDispatchers,
    private val mapper: JupiterSwapRoutesMapper,
) : JupiterSwapRoutesRepository {

    override suspend fun getSwapRoutes(
        jupiterSwap: JupiterSwap,
        userPublicKey: Base58String
    ): List<SwapRoute> = with(dispatchers.io) {
        val response = api.getSwapRoutes(
            inputMint = jupiterSwap.inputMint.base58Value,
            outputMint = jupiterSwap.outputMint.base58Value,
            amountInLamports = jupiterSwap.amountInLamports,
            userPublicKey = userPublicKey.base58Value
        )
        mapper.fromNetwork(response)
    }
}

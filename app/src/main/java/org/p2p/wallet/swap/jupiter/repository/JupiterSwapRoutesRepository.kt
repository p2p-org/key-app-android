package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterApi
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwap
import org.p2p.wallet.swap.jupiter.repository.model.SwapRoute
import org.p2p.wallet.utils.Base58String

class JupiterSwapRoutesRepository(
    private val api: SwapJupiterApi,
    private val mapper: JupiterSwapRoutesMapper,
) : SwapRoutesRepository {

    override suspend fun getSwapRoutes(jupiterSwap: JupiterSwap, userPublicKey: Base58String): List<SwapRoute> =
        mapper.toSwapRoute(
            api.getSwapRoutes(
                inputMint = jupiterSwap.inputMint,
                outputMint = jupiterSwap.outputMint,
                amount = jupiterSwap.amount,
                userPublicKey = userPublicKey
            )
        )
}

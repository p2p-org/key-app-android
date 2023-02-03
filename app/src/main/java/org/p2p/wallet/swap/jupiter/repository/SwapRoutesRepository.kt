package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwap
import org.p2p.wallet.swap.jupiter.repository.model.SwapRoute
import org.p2p.wallet.utils.Base58String

interface SwapRoutesRepository {
    suspend fun getSwapRoutes(jupiterSwap: JupiterSwap, userPublicKey: Base58String): List<SwapRoute>
}

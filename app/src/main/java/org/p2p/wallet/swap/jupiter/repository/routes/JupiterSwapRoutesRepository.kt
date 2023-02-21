package org.p2p.wallet.swap.jupiter.repository.routes

import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwap
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.utils.Base58String

interface JupiterSwapRoutesRepository {
    suspend fun getSwapRoutes(jupiterSwap: JupiterSwap, userPublicKey: Base58String): List<JupiterSwapRoute>
    suspend fun loadAllSwapRoutes()
    suspend fun getSwappableTokenMints(sourceTokenMint: Base58String): List<Base58String>
}

package org.p2p.wallet.jupiter.repository.routes

import org.p2p.core.crypto.Base58String
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute

interface JupiterSwapRoutesRepository {
    /**
     * @param validateRoutes - if true validate routes and return only valid ones
     */
    suspend fun getSwapRoutesForSwapPair(
        jupiterSwapPair: JupiterSwapPair,
        userPublicKey: Base58String,
        validateRoutes: Boolean = true
    ): List<JupiterSwapRoute>
    suspend fun loadAndCacheAllSwapRoutes()
    suspend fun getSwappableTokenMints(sourceTokenMint: Base58String): List<Base58String>
}

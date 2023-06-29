package org.p2p.wallet.jupiter.repository.routes

import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.core.crypto.Base58String

interface JupiterSwapRoutesRepository {
    suspend fun getSwapRoutesForSwapPair(
        jupiterSwapPair: JupiterSwapPair,
        userPublicKey: Base58String
    ): List<JupiterSwapRoute>
    suspend fun loadAndCacheAllSwapRoutes()
    suspend fun getSwappableTokenMints(sourceTokenMint: Base58String): List<Base58String>
}

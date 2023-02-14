package org.p2p.wallet.swap.jupiter.repository.routes

import org.p2p.wallet.utils.Base58String

interface JupiterSwapRoutesLocalRepository {
    suspend fun getSwappableTokenMints(sourceTokenMint: Base58String): List<Base58String>
    fun setCachedSwapRoutes(swapRoutes: JupiterAvailableSwapRoutesMap)
}

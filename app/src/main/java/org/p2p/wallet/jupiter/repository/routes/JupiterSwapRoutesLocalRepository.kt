package org.p2p.wallet.jupiter.repository.routes

interface JupiterSwapRoutesLocalRepository {
    fun setCachedSwapRoutes(swapRoutes: JupiterAvailableSwapRoutesMap)
    fun getCachedAllSwapRoutes(): JupiterAvailableSwapRoutesMap?
}

package org.p2p.wallet.jupiter.repository.routes

import timber.log.Timber

internal class JupiterSwapRoutesInMemoryRepository : JupiterSwapRoutesLocalRepository {

    private var cachedAllSwapRoutes: JupiterAvailableSwapRoutesMap? = null
        set(value) {
            Timber.i("allSwapRoutes updated: old:${field?.allRoutes?.size}; new=${value?.allRoutes?.size}")
            field = value
        }

    override fun getCachedAllSwapRoutes(): JupiterAvailableSwapRoutesMap? = cachedAllSwapRoutes

    override fun setCachedSwapRoutes(swapRoutes: JupiterAvailableSwapRoutesMap) {
        this.cachedAllSwapRoutes = swapRoutes
    }
}

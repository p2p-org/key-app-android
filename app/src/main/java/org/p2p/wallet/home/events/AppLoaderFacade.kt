package org.p2p.wallet.home.events

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.p2p.core.common.di.AppScope

class AppLoaderFacade(
    private val appLoaders: List<AppLoader>,
    private val appScope: AppScope
) : AppLoader {

    override suspend fun onLoad() {
        appLoaders.filter { it.isEnabled() }
            .map { appScope.async { it.onLoad() } }
            .awaitAll()
    }

    override suspend fun onRefresh() {
        appLoaders.filter { it.isEnabled() }
            .map { appScope.async { it.onRefresh() } }
            .awaitAll()
    }
}

package org.p2p.wallet.home.events

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.core.common.di.AppScope

class AppLoaderFacade(
    private val appLoaders: List<AppLoader>,
    private val appScope: AppScope
) : AppLoader {

    override suspend fun onLoad() {
        withContext(appScope.coroutineContext) {
            appLoaders.filter { it.isEnabled() }
                .map { appScope.async { it.onLoad() } }
                .awaitAll()

        }
    }

    override suspend fun isEnabled(): Boolean = true

    override suspend fun onRefresh() {
        withContext(appScope.coroutineContext) {
            appLoaders.filter { it.isEnabled() }
                .map { appScope.async { it.onLoad() } }
                .awaitAll()
        }
    }
}

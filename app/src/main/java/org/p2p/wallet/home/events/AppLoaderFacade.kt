package org.p2p.wallet.home.events

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.p2p.core.common.di.AppScope

/**
 * This is parent of [AppLoader] classes,
 * which contains all [AppLoader] instances,which are hidden by this facade.
 * Now we can just add new [AppLoader] instance and it will be automatically connected to the
 * app loading process
 */
class AppLoaderFacade(
    private val appLoaders: List<AppLoader>,
    private val appScope: AppScope
) {

    /**
     * Keys are root loaders, values are dependants
     * It means, the keys must be loaded firstly, then the values and so on recursively
     */
    private val dependencyGraph: Map<AppLoader, List<AppLoader>> = buildDependencyGraph()

    suspend fun onLoad() {
        processLoaders()
    }

    @Suppress("DeferredResultUnused")
    suspend fun onRefresh() {
        appLoaders.filter { it.isEnabled() }
            .forEach { appScope.async { it.onRefresh() } }
    }

    private suspend fun processLoaders() {
        val processedLoaders = mutableMapOf<String, Deferred<Unit>>()

        for (rootLoader in dependencyGraph.keys) {
            processLoader(rootLoader, processedLoaders)
        }
    }

    private suspend fun processLoader(
        rootLoader: AppLoader,
        processedLoaders: MutableMap<String, Deferred<Unit>>,
    ) {
        val existingLoading = processedLoaders[rootLoader.id()]
        if (existingLoading != null) {
            if (existingLoading.isActive) {
                existingLoading.await()
            }
            return
        }

        // get nested AppLoaders
        val dependencies = dependencyGraph[rootLoader] ?: emptyList()

        dependencies.forEach { dependency ->
            processLoader(dependency, processedLoaders)
        }

        if (rootLoader.isEnabled()) {
            processedLoaders[rootLoader.id()] = appScope.async {
                rootLoader.onLoad()
            }
        }
    }

    private fun buildDependencyGraph(): Map<AppLoader, List<AppLoader>> {
        val dependencyGraph = mutableMapOf<AppLoader, List<AppLoader>>()

        appLoaders.forEach { loader ->
            val dependencies = loader.getDependencies().mapNotNull { dependency ->
                appLoaders.find { it.id() == dependency.id() }
            }
            dependencyGraph[loader] = dependencies
        }

        return dependencyGraph
    }

    private fun AppLoader.id(): String = javaClass.name
}

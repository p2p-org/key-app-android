package org.p2p.wallet.home.events

abstract class AppLoader {
    private val dependencies: MutableSet<AppLoader> = mutableSetOf()
    abstract suspend fun onLoad()
    open suspend fun onRefresh(): Unit = Unit
    open suspend fun isEnabled(): Boolean = true

    fun getDependencies(): Set<AppLoader> = dependencies

    fun dependsOn(vararg loaders: AppLoader) {
        dependencies += loaders
    }
}

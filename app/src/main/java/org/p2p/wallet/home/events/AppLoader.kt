package org.p2p.wallet.home.events

interface AppLoader {
    suspend fun onLoad()
    suspend fun onRefresh(): Unit = Unit
    suspend fun isEnabled(): Boolean = true
}

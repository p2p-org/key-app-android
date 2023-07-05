package org.p2p.wallet.home.events

interface HomeScreenLoader {
    suspend fun onLoad()
    suspend fun onRefresh()
}

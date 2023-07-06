package org.p2p.wallet.home.events

import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.network.ConnectionManager
import org.p2p.wallet.updates.SocketState
import org.p2p.wallet.updates.SocketUpdatesManager
import org.p2p.wallet.updates.SubscriptionUpdatesStateObserver
import org.p2p.wallet.updates.subscribe.SubscriptionUpdateSubscriber

class SocketSubscribeLoader(
    private val updatesManager: SocketUpdatesManager,
    private val updateSubscribers: List<SubscriptionUpdateSubscriber>,
    private val connectionManager: ConnectionManager,
    private val appScope: AppScope
) : HomeScreenLoader {

    override suspend fun onLoad() {
        updatesManager.addUpdatesStateObserver(object : SubscriptionUpdatesStateObserver {
            override fun onUpdatesStateChanged(state: SocketState) {
                if (state == SocketState.CONNECTED) {
                    updateSubscribers.forEach {
                        it.subscribe()
                    }
                }
            }
        })

        updatesManager.start()
    }

    private fun observeConnectionStatus() = appScope.launch {
        connectionManager.connectionStatus.collect { hasConnection ->
            if (hasConnection) {
                if (!updatesManager.isStarted()) {
                    updatesManager.restart()
                }
            } else if (updatesManager.isStarted()) {
                updatesManager.stop()
            }
        }
    }
}

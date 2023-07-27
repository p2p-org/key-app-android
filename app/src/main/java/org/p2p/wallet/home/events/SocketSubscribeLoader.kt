package org.p2p.wallet.home.events

import org.p2p.wallet.updates.SocketState
import org.p2p.wallet.updates.SocketUpdatesManager
import org.p2p.wallet.updates.SubscriptionUpdatesStateObserver
import org.p2p.wallet.updates.subscribe.SubscriptionUpdateSubscriber

class SocketSubscribeLoader(
    private val updatesManager: SocketUpdatesManager,
    private val updateSubscribers: List<SubscriptionUpdateSubscriber>,
) : AppLoader() {

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
}

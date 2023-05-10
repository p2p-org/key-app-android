package org.p2p.wallet.updates

interface SubscriptionUpdatesStateObserver {

    companion object {
        operator fun invoke(updateHandler: (SocketState) -> Unit) = object : SubscriptionUpdatesStateObserver {
            override fun onUpdatesStateChanged(state: SocketState) {
                updateHandler(state)
            }
        }
    }

    fun onUpdatesStateChanged(state: SocketState)
}

package org.p2p.wallet.updates

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest

interface SubscriptionUpdatesManager {
    fun start()

    /**
     * Stops updates manager. This action is terminal and updates manager can't be restarted after it.
     * If you need [stop]-[start] cycle - then use [restart] instead. It uses non-terminal stop inside.
     */
    fun stop()

    /**
     * Suspends until socket updates are stopped in non-terminal manner, then queues up [start] and returns.
     */
    suspend fun restart()

    /**
     * Check if updates manager is already started.
     */
    fun isStarted(): Boolean

    fun addUpdatesStateObserver(observer: SubscriptionUpdatesStateObserver)

    fun removeUpdatesStateObserver(observer: SubscriptionUpdatesStateObserver)

    fun addSubscription(request: RpcRequest, updateType: SocketSubscriptionUpdateType)

    fun addSubscription(request: RpcMapRequest, updateType: SocketSubscriptionUpdateType)

    fun removeSubscription(request: RpcRequest)

    fun removeSubscription(request: RpcMapRequest)
}

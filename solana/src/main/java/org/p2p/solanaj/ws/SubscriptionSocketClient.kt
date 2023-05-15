package org.p2p.solanaj.ws

import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest

interface SubscriptionSocketClient {
    val isSocketOpen: Boolean

    fun ping()
    fun addSubscription(request: RpcMapRequest, listener: SubscriptionEventListener)
    fun addSubscription(request: RpcRequest, listener: SubscriptionEventListener)
    fun removeSubscription(request: RpcMapRequest)
    fun removeSubscription(request: RpcRequest)
    fun connect()
    fun close()
    fun reconnect()
}

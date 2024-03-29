package org.p2p.wallet.updates

import com.google.gson.JsonObject

interface SubscriptionUpdateHandler {
    suspend fun initialize()
    suspend fun onUpdate(type: SocketSubscriptionUpdateType, data: JsonObject)
}

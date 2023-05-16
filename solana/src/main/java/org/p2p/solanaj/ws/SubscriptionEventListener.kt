package org.p2p.solanaj.ws

import com.google.gson.JsonObject

fun interface SubscriptionEventListener {
    fun onSubscriptionUpdated(newData: JsonObject)
}

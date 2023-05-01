package org.p2p.solanaj.ws

import com.google.gson.JsonObject

fun interface NotificationEventListener {
    fun onNotificationEvent(data: JsonObject)
}

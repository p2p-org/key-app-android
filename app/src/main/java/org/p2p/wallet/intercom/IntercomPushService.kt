package org.p2p.wallet.intercom

import com.google.firebase.messaging.RemoteMessage

class IntercomPushService() {

    fun registerForPush(userToken: String) = Unit

    fun isIntercomPush(message: RemoteMessage): Boolean = false

    fun handlePush(message: RemoteMessage) = Unit
}

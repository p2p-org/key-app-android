package org.p2p.wallet.intercom

import android.app.Application
import com.google.firebase.messaging.RemoteMessage
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.push.IntercomPushClient

class IntercomPushService(
    private val application: Application,
) {

    private val intercomClient: Intercom
        get() = Intercom.client()

    private val pushClient: IntercomPushClient = IntercomPushClient()

    fun registerForPush(userToken: String) {
        pushClient.sendTokenToIntercom(application, userToken)
    }

    fun isIntercomPush(message: RemoteMessage): Boolean = pushClient.isIntercomPush(message.data)

    fun handlePush(message: RemoteMessage) {
        pushClient.handlePush(application, message.data)
    }

    fun showPushContentIfExists() {
        intercomClient.handlePushMessage()
    }
}

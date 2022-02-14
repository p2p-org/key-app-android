package org.p2p.wallet.intercom

import android.app.Application
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.identity.Registration

object IntercomService {

    fun setup(app: Application, apiKey: String, appId: String) {
        Intercom.initialize(app, apiKey, appId)
        // Hide in app messages
        Intercom.client().setInAppMessageVisibility(Intercom.Visibility.GONE)
    }

    fun signIn(userId: String, onMessageReceived: (count: Int) -> Unit) {
        if (userId.isNotEmpty()) {
            val registration = Registration.create().withUserId(userId)
            Intercom.client().registerIdentifiedUser(registration)
        } else {
            Intercom.client().registerUnidentifiedUser()
        }
        Intercom.client().addUnreadConversationCountListener { count ->
            onMessageReceived.invoke(count)
        }
    }

    fun logout() {
        Intercom.client().logout()
    }

    fun showMessenger() {
        Intercom.client().displayMessenger()
    }
}
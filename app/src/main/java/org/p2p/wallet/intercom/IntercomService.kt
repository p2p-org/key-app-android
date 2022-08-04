package org.p2p.wallet.intercom

import android.app.Application
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.identity.Registration
import timber.log.Timber

object IntercomService {
    private const val TAG = "IntercomService"

    fun setup(app: Application, apiKey: String, appId: String) {
        Timber.tag(TAG).i("Intercom client setup.")
        Intercom.initialize(app, apiKey, appId)
        // Hide in app messages
        Intercom.client().setInAppMessageVisibility(Intercom.Visibility.GONE)

        Timber.tag(TAG).i("Intercom client setup finish. Client used: ${Intercom.client()}")
    }

    fun signIn(userId: String) {
        if (userId.isNotEmpty()) {
            Timber.tag(TAG).i("Intercom client signing in. IdentifiedUser")
            val registration = Registration.create().withUserId(userId)
            Intercom.client().registerIdentifiedUser(registration)
        } else {
            Timber.tag(TAG).i("Intercom client signing in. UnidentifiedUser")
            Intercom.client().registerUnidentifiedUser()
        }
    }

    fun logout() {
        Timber.tag(TAG).i("Intercom client logout.")
        Intercom.client().logout()
    }

    fun showMessenger() {
        Timber.tag(TAG).i("Intercom client displays messenger.")
        Intercom.client().displayMessenger()
    }
}

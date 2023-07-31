package org.p2p.wallet.intercom

import android.app.Application
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.identity.Registration
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.core.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import timber.log.Timber

object IntercomService : KoinComponent {
    private const val TAG = "IntercomService"

    private val screenAnalyticsInteractor: ScreensAnalyticsInteractor by inject()

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
            Intercom.client().loginIdentifiedUser(registration)
        } else {
            Timber.tag(TAG).i("Intercom client signing in. UnidentifiedUser")
            Intercom.client().loginUnidentifiedUser()
        }
    }

    fun logout() {
        Timber.tag(TAG).i("Intercom client logout.")
        Intercom.client().logout()
    }

    fun showMessenger() {
        screenAnalyticsInteractor.logScreenOpenEvent(ScreenNames.Main.MAIN_FEEDBACK)

        Timber.tag(TAG).i("Intercom client displays messenger.")
        Intercom.client().present()
    }
}

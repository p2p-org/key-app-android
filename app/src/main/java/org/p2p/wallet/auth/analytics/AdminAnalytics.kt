package org.p2p.wallet.auth.analytics

import org.p2p.core.analytics.Analytics
import org.p2p.core.analytics.constants.EventNames.ADMIN_APP_CLOSED
import org.p2p.core.analytics.constants.EventNames.ADMIN_APP_OPENED
import org.p2p.core.analytics.constants.EventNames.ADMIN_PASSWORD_CREATED
import org.p2p.core.analytics.constants.EventNames.ADMIN_PIN_CREATED
import org.p2p.core.analytics.constants.EventNames.ADMIN_PIN_REJECTED
import org.p2p.core.analytics.constants.EventNames.ADMIN_PIN_RESET_INVOKED
import org.p2p.core.analytics.constants.EventNames.ADMIN_PIN_RESET_VALIDATED
import org.p2p.core.analytics.constants.EventNames.ADMIN_PUSH_RECEIVED
import org.p2p.core.analytics.constants.EventNames.ADMIN_SIGNED_OUT
import org.p2p.core.analytics.constants.EventNames.ADMIN_SIGN_OUT
import org.p2p.core.analytics.constants.EventNames.ADMIN_SNACKBAR_RECEIVED

class AdminAnalytics(private val tracker: Analytics) {

    fun logAppOpened(source: AppOpenSource) {
        tracker.logEvent(
            event = ADMIN_APP_OPENED,
            params = mapOf("Source_Open" to source.sourceName)
        )
    }

    fun logAppClosed(lastScreenName: String) {
        tracker.logEvent(
            event = ADMIN_APP_CLOSED,
            params = mapOf("Last_Screen" to lastScreenName)
        )
    }

    fun logPushReceived(campaignName: String) {
        tracker.logEvent(ADMIN_PUSH_RECEIVED, arrayOf(Pair("Push_Campaign", campaignName)))
    }

    fun logSnackBarReceived(message: String) {
        tracker.logEvent(ADMIN_SNACKBAR_RECEIVED, arrayOf(Pair("Snackbar_Type", message)))
    }

    fun logSignOut() {
        tracker.logEvent(ADMIN_SIGN_OUT)
    }

    fun logSignedOut(backupState: BackupState = BackupState.OFF) {
        tracker.logEvent(ADMIN_SIGNED_OUT, arrayOf(Pair("Backup_State", backupState.title)))
    }

    // TODO determine about pin complex calculation
    fun logPinCreated(isPinComplex: Boolean = false, currentScreenName: String) {
        tracker.logEvent(
            ADMIN_PIN_CREATED,
            arrayOf(
                Pair("Pin_Complexity", isPinComplex),
                Pair("Current_Screen", currentScreenName)
            )
        )
    }

    fun logPinRejected(currentScreenName: String) {
        tracker.logEvent(
            ADMIN_PIN_REJECTED,
            arrayOf(
                Pair("Current_Screen", currentScreenName)
            )
        )
    }

    fun logPinResetValidated(authResult: AuthAnalytics.AuthResult) {
        tracker.logEvent(
            ADMIN_PIN_RESET_VALIDATED,
            arrayOf(
                Pair("Auth_Reset_Result", authResult.title)
            )
        )
    }

    fun logPinResetInvoked() {
        tracker.logEvent(ADMIN_PIN_RESET_INVOKED)
    }

    fun logPasswordCreated() {
        tracker.logEvent(ADMIN_PASSWORD_CREATED)
    }

    enum class AppOpenSource(val sourceName: String) {
        DIRECT("Direct"),
        DEEPLINK("Deeplink"),
        PUSH("Push")
    }

    enum class BackupState(val title: String) {
        ON("On"),
        OFF("Off"),
        DISCARDED("Discarded"),
        NO_NEED("No_Need")
    }
}

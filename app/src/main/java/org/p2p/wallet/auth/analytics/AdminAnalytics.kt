package org.p2p.wallet.auth.analytics

import org.p2p.wallet.common.analytics.TrackerContract

class AdminAnalytics(
    private val tracker: TrackerContract
) {

    fun logAppOpened(source: AppOpenSource) {
        tracker.logEvent("App_Opened", arrayOf(Pair("Source_Open", source.title)))
    }

    fun logAppClosed(lastScreenName: String) {
        tracker.logEvent("App_Closed", arrayOf(Pair("Last_Screen", lastScreenName)))
    }

    fun logPushReceived(campaignName: String) {
        tracker.logEvent("Push_Received", arrayOf(Pair("Push_Campaign", campaignName)))
    }

    fun logSnackBarReceived(message: String) {
        tracker.logEvent("Snackbar_Received", arrayOf(Pair("Snackbar_Type", message)))
    }

    fun logSignOut(backupState: BackupState = BackupState.OFF, lastScreen: String) {
        tracker.logEvent(
            "Sign_Out",
            arrayOf(
                Pair("Backup_State", backupState.title),
                Pair("Last_Screen", lastScreen)
            )
        )
    }

    fun logSignedOut(backupState: BackupState = BackupState.OFF) {
        tracker.logEvent("Signed_Out", arrayOf(Pair("Backup_State", backupState.title)))
    }

    // TODO determine about pin complex calculation
    fun logPinCreated(isPinComplex: Boolean = false) {
        tracker.logEvent("Pin_Created", arrayOf(Pair("Pin_Complexity", isPinComplex)))
    }

    fun logPasswordCreated() {
        tracker.logEvent("Password_Created")
    }

    enum class AppOpenSource(val title: String) {
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
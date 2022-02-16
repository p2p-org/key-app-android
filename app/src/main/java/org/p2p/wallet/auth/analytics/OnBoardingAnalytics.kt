package org.p2p.wallet.auth.analytics

import org.p2p.wallet.common.analytics.TrackerContract

class OnBoardingAnalytics(
    private val tracker: TrackerContract
) {
    fun logSplashViewed(campaignName: String) {
        tracker.logEvent(
            "Splash_Viewed",
            arrayOf(
                Pair("Splash_Campaign", campaignName)
            )
        )
    }

    fun logSplashSwiped(campaignName: String) {
        tracker.logEvent(
            "Splash_Swiped",
            arrayOf(
                Pair("Swipe_Campaign", campaignName)
            )
        )
    }

    fun logSplashRestored() {
        tracker.logEvent("Splash_Restoring")
    }

    fun logSplashCreating() {
        tracker.logEvent("Splash_Creating")
    }

    fun logRestoreGoogleInvoked() {
        tracker.logEvent("Restore_Google_Invoked")
    }

    fun logRestoreManualInvoked() {
        tracker.logEvent("Restore_Manual_Invoked")
    }

    fun logCreateManualInvoked() {
        tracker.logEvent("Create_Manual_Invoked")
    }

    fun logCreateSeedInvoked() {
        tracker.logEvent("Create_Seed_Invoked")
    }

    fun logBackingUpCopying() {
        tracker.logEvent("Backing_Up_Copying")
    }

    fun logBackingUpSaving() {
        tracker.logEvent("Backing_Up_Saving")
    }

    fun logBackingUpRenew() {
        tracker.logEvent("Backing_Up_Renewing")
    }

    fun logBackingUpManually() {
        tracker.logEvent("Backing_Up_Manually")
    }

    fun logBackingUpError() {
        tracker.logEvent("Backing_Up_Error")
    }

    fun logBioRejected() {
        tracker.logEvent("Bio_Rejected")
    }

    fun logWalletCreated(lastScreenName: String) {
        tracker.logEvent(
            "Wallet_Created",
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logWalletRestored(lastScreenName: String) {
        tracker.logEvent(
            "Wallet_Restored",
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logManyWalletFound(lastScreenName: String) {
        tracker.logEvent(
            "Many_Wallets_Found",
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logNoWalletFound(lastScreenName: String) {
        tracker.logEvent(
            "No_Wallet_Found",
            arrayOf(
                Pair("No_Wallet_Found", lastScreenName)
            )
        )
    }

    fun logBioApproved() {
        tracker.logEvent("Bio_Approved")
    }

    fun logPushRejected() {
        tracker.logEvent("Push_Rejected")
    }

    fun logPushApproved() {
        tracker.logEvent("Push_Approved")
    }

    fun logUsernameSkipped(usernameField: UsernameField) {
        tracker.logEvent(
            "Username_Skipped",
            arrayOf(
                Pair("Username_Field", usernameField.title)
            )
        )
    }

    fun logUsernameSaved(lastScreenName: String) {
        tracker.logEvent(
            "Username_Saved",
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logUsernameReserved() {
        tracker.logEvent("Username_Reserved")
    }

    enum class UsernameField(val title: String) {
        FILLED("Filled"),
        NOT_FILLED("Not_Filled");

        companion object {
            fun getValueOf(username: String): UsernameField {
                return if (username.isEmpty()) FILLED else NOT_FILLED
            }
        }
    }
}
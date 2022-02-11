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

    fun logBioRejected() {
        tracker.logEvent("Bio_Rejected")
    }

    fun logBioApproved(lastScreenName: String) {
        tracker.logEvent(
            "Bio_Approved",
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
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

    enum class UsernameField(val title: String) {
        FILLED("Filled"),
        NOT_FILLED("Not_Filled")
    }
}
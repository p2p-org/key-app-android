package org.p2p.wallet.auth.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_BACKING_UP_COPYING
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_BACKING_UP_RENEW
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_BACKING_UP_SAVING
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_BACKUP_ERROR
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_BACKUP_MANUALLY
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_BIO_APPROVED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_BIO_REJECTED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_CREATE_MANUAL_INVOKED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_CREATE_SEED_INVOKED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_MANY_WALLETS_FOUND
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_NO_WALLET_FOUND
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_PUSH_APPROVED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_PUSH_REJECTED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_RESTORE_GOOGLE_INVOKED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_RESTORE_MANUAL_INVOKED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_SPLASH_CREATED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_SPLASH_RESTORING
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_SPLASH_SWIPED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_SPLASH_VIEWED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_USERNAME_RESERVED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_USERNAME_SAVED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_USERNAME_SKIPPED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_WALLET_CREATED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_WALLET_RESTORED

class OnBoardingAnalytics(private val tracker: Analytics) {
    fun logSplashViewed(campaignName: String) {
        tracker.logEvent(
            ONBOARD_SPLASH_VIEWED,
            arrayOf(
                Pair("Splash_Campaign", campaignName)
            )
        )
    }

    fun logSplashSwiped(campaignName: String) {
        tracker.logEvent(
            ONBOARD_SPLASH_SWIPED,
            arrayOf(
                Pair("Swipe_Campaign", campaignName)
            )
        )
    }

    fun logSplashRestored() {
        tracker.logEvent(ONBOARD_SPLASH_RESTORING)
    }

    fun logSplashCreating() {
        tracker.logEvent(ONBOARD_SPLASH_CREATED)
    }

    fun logRestoreGoogleInvoked() {
        tracker.logEvent(ONBOARD_RESTORE_GOOGLE_INVOKED)
    }

    fun logRestoreManualInvoked() {
        tracker.logEvent(ONBOARD_RESTORE_MANUAL_INVOKED)
    }

    fun logCreateManualInvoked() {
        tracker.logEvent(ONBOARD_CREATE_MANUAL_INVOKED)
    }

    fun logCreateSeedInvoked() {
        tracker.logEvent(ONBOARD_CREATE_SEED_INVOKED)
    }

    fun logBackingUpCopying() {
        tracker.logEvent(ONBOARD_BACKING_UP_COPYING)
    }

    fun logBackingUpSaving() {
        tracker.logEvent(ONBOARD_BACKING_UP_SAVING)
    }

    fun logBackingUpRenew() {
        tracker.logEvent(ONBOARD_BACKING_UP_RENEW)
    }

    fun logBackingUpManually() {
        tracker.logEvent(ONBOARD_BACKUP_MANUALLY)
    }

    fun logBackingUpError() {
        tracker.logEvent(ONBOARD_BACKUP_ERROR)
    }

    fun logBioRejected() {
        tracker.logEvent(ONBOARD_BIO_REJECTED)
    }

    fun logWalletCreated(lastScreenName: String) {
        tracker.logEvent(
            ONBOARD_WALLET_CREATED,
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logWalletRestored(lastScreenName: String) {
        tracker.logEvent(
            ONBOARD_WALLET_RESTORED,
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logManyWalletFound(lastScreenName: String) {
        tracker.logEvent(
            ONBOARD_MANY_WALLETS_FOUND,
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logNoWalletFound(lastScreenName: String) {
        tracker.logEvent(
            ONBOARD_NO_WALLET_FOUND,
            arrayOf(
                Pair("No_Wallet_Found", lastScreenName)
            )
        )
    }

    fun logBioApproved() {
        tracker.logEvent(ONBOARD_BIO_APPROVED)
    }

    fun logPushRejected() {
        tracker.logEvent(ONBOARD_PUSH_REJECTED)
    }

    fun logPushApproved() {
        tracker.logEvent(ONBOARD_PUSH_APPROVED)
    }

    fun logUsernameSkipped(usernameField: UsernameField) {
        tracker.logEvent(
            ONBOARD_USERNAME_SKIPPED,
            arrayOf(
                Pair("Username_Field", usernameField.title)
            )
        )
    }

    fun logUsernameSaved(lastScreenName: String) {
        tracker.logEvent(
            ONBOARD_USERNAME_SAVED,
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logUsernameReserved() {
        tracker.logEvent(ONBOARD_USERNAME_RESERVED)
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

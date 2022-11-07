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
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_CREATE_PHONE_CLICK_BUTTON
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_CREATE_SEED_INVOKED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_CREATE_SMS_SCREEN
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_CREATE_SMS_VALIDATION
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_CREATE_WALLET_CONFIRM_PIN
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_CREATION_PHONE_SCREEN
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_MANY_WALLETS_FOUND
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_NO_WALLET_FOUND
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_PROPERTY_USER_DEVICE_SHARE
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_PROPERTY_USER_RESTORE_METHOD
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_PUSH_APPROVED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_PUSH_REJECTED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_RESTORE_GOOGLE_INVOKED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_RESTORE_MANUAL_INVOKED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_RESTORE_PHONE_SCREEN
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_RESTORE_SMS_PHONE_CLICK_BUTTON
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_RESTORE_SMS_SCREEN
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_RESTORE_SMS_VALIDATION
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_RESTORE_WALLET_BUTTON
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_RESTORE_WALLET_CONFIRM_PIN
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_SELECT_RESTORE_OPTION
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_SPLASH_CREATED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_SPLASH_RESTORING
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_SPLASH_SWIPED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_SPLASH_VIEWED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_START_BUTTON
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_TORUS_REQUEST
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_USERNAME_RESERVED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_USERNAME_SAVED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_USERNAME_SKIPPED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_WALLET_CREATED
import org.p2p.wallet.common.analytics.constants.EventNames.ONBOARD_WALLET_RESTORED
import org.p2p.wallet.utils.emptyString
import kotlin.time.Duration

class OnboardingAnalytics(private val tracker: Analytics) {
    fun logSplashViewed() {
        tracker.logEvent(
            event = ONBOARD_SPLASH_VIEWED,
            params = mapOf("Splash_Campaign" to emptyString())
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
            event = ONBOARD_WALLET_CREATED,
            params = mapOf(
                "Last_Screen" to lastScreenName
            )
        )
    }

    fun logWalletRestored(lastScreenName: String) {
        tracker.logEvent(
            event = ONBOARD_WALLET_RESTORED,
            params = mapOf(
                "Last_Screen" to lastScreenName
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

    fun logCreateWalletClicked() {
        tracker.logEvent(event = ONBOARD_START_BUTTON)
    }

    fun logCreateWalletPinConfirmed() {
        tracker.logEvent(
            event = ONBOARD_CREATE_WALLET_CONFIRM_PIN,
            params = mapOf(
                "Result" to true
            )
        )
    }

    fun logRestoreWalletPinConfirmed() {
        tracker.logEvent(
            event = ONBOARD_RESTORE_WALLET_CONFIRM_PIN,
            params = mapOf(
                "Result" to true
            )
        )
    }

    fun logAlreadyHaveWalletClicked() {
        tracker.logEvent(event = ONBOARD_RESTORE_WALLET_BUTTON)
    }

    fun logRestoreOptionClicked(restoreWay: AnalyticsRestoreWay) {
        tracker.logEvent(
            event = ONBOARD_SELECT_RESTORE_OPTION,
            params = mapOf(
                "Restore_Option" to restoreWay.value
            )
        )
    }

    fun logPhoneEnterScreenOpened() {
        tracker.logEvent(ONBOARD_CREATION_PHONE_SCREEN)
    }

    fun logTorusRequestResponseTime(
        methodName: String,
        responseDuration: Duration
    ) {
        tracker.logEvent(
            event = ONBOARD_TORUS_REQUEST,
            params = mapOf(
                "Method_Name" to methodName,
                "Minutes" to responseDuration.inWholeMinutes,
                "Seconds" to responseDuration.inWholeSeconds,
                "Milliseconds" to responseDuration.inWholeMilliseconds
            )
        )
    }

    fun logConfirmPhoneButtonClicked() {
        tracker.logEvent(ONBOARD_CREATE_PHONE_CLICK_BUTTON)
    }

    fun logCreateSmsInputScreenOpened() {
        tracker.logEvent(ONBOARD_CREATE_SMS_SCREEN)
    }

    fun logSmsValidationResult(isSmsValid: Boolean) {
        tracker.logEvent(
            event = ONBOARD_CREATE_SMS_VALIDATION,
            params = mapOf(
                "Result" to isSmsValid
            )
        )
    }

    fun logRestorePhoneEnterScreenOpened() {
        tracker.logEvent(ONBOARD_RESTORE_PHONE_SCREEN)
    }

    fun logRestoreConfirmPhoneButtonClicked() {
        tracker.logEvent(ONBOARD_RESTORE_SMS_PHONE_CLICK_BUTTON)
    }

    fun logRestoreSmsInputScreenOpened() {
        tracker.logEvent(ONBOARD_RESTORE_SMS_SCREEN)
    }

    fun logRestoreSmsValidationResult(isSmsValid: Boolean) {
        tracker.logEvent(
            event = ONBOARD_RESTORE_SMS_VALIDATION,
            params = mapOf(
                "Result" to isSmsValid
            )
        )
    }

    fun setUserRestoreMethod(restoreMethod: UsernameRestoreMethod) {
        tracker.setUserProperty(
            key = ONBOARD_PROPERTY_USER_RESTORE_METHOD,
            value = restoreMethod.analyticValue
        )
    }

    fun setUserHasDeviceShare(hasDeviceShare: Boolean) {
        tracker.setUserProperty(
            key = ONBOARD_PROPERTY_USER_DEVICE_SHARE,
            value = hasDeviceShare
        )
    }

    enum class UsernameRestoreMethod(val analyticValue: String) {
        WEB3AUTH("web3auth"),
        SEED_PHRASE("seed_phrase")
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

    enum class AnalyticsRestoreWay(val value: String) {
        PHONE("phone"), GOOGLE("google"), SEED("seed")
    }
}

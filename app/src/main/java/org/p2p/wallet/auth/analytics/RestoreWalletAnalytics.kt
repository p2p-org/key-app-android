package org.p2p.wallet.auth.analytics

import org.p2p.core.analytics.Analytics

private const val ONBOARDING_PROPERTY_USER_RESTORE_METHOD = "User_Restore_Method"
private const val ONBOARDING_RESTORE_WALLET_CONFIRM_PIN = "Restore_Confirm_Pin"
private const val ONBOARDING_RESTORE_WALLET_BUTTON = "Restore_Wallet_Button"
private const val ONBOARDING_SELECT_RESTORE_OPTION = "Select_Restore_Option"
private const val ONBOARDING_RESTORE_PHONE_SCREEN = "Restore_Phone_Screen"
private const val ONBOARDING_RESTORE_SMS_PHONE_CLICK_BUTTON = "Restore_Phone_Click_Button"
private const val ONBOARDING_RESTORE_SMS_SCREEN = "Restore_Sms_Screen"
private const val ONBOARDING_RESTORE_SMS_VALIDATION = "Restore_Sms_Validation"

class RestoreWalletAnalytics(
    private val tracker: Analytics
) {
    fun logRestoreWalletPinConfirmed() {
        tracker.logEvent(
            event = ONBOARDING_RESTORE_WALLET_CONFIRM_PIN,
            params = mapOf(
                "Result" to true
            )
        )
    }

    fun logAlreadyHaveWalletClicked() {
        tracker.logEvent(event = ONBOARDING_RESTORE_WALLET_BUTTON)
    }

    fun logRestoreOptionClicked(restoreWay: AnalyticsRestoreWay) {
        tracker.logEvent(
            event = ONBOARDING_SELECT_RESTORE_OPTION,
            params = mapOf(
                "Restore_Option" to restoreWay.value
            )
        )
    }

    fun logRestorePhoneEnterScreenOpened() {
        tracker.logEvent(ONBOARDING_RESTORE_PHONE_SCREEN)
    }

    fun logRestoreConfirmPhoneButtonClicked() {
        tracker.logEvent(ONBOARDING_RESTORE_SMS_PHONE_CLICK_BUTTON)
    }

    fun logRestoreSmsInputScreenOpened() {
        tracker.logEvent(ONBOARDING_RESTORE_SMS_SCREEN)
    }

    fun logRestoreSmsValidationResult(isSmsValid: Boolean) {
        tracker.logEvent(
            event = ONBOARDING_RESTORE_SMS_VALIDATION,
            params = mapOf(
                "Result" to isSmsValid
            )
        )
    }

    fun setUserRestoreMethod(restoreMethod: UsernameRestoreMethod) {
        tracker.setUserProperty(
            key = ONBOARDING_PROPERTY_USER_RESTORE_METHOD,
            value = restoreMethod.analyticValue
        )
        tracker.logEvent(
            event = ONBOARDING_PROPERTY_USER_RESTORE_METHOD,
            params = arrayOf("Restore_Method" to restoreMethod.analyticValue)
        )
    }

    enum class UsernameRestoreMethod(val analyticValue: String) {
        WEB3AUTH("web3auth"),
        SEED_PHRASE("seed_phrase")
    }

    enum class AnalyticsRestoreWay(val value: String) {
        PHONE("phone"), GOOGLE("google"), SEED("seed")
    }
}

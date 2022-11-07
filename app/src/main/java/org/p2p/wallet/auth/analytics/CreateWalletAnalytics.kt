package org.p2p.wallet.auth.analytics

import org.p2p.wallet.common.analytics.Analytics

private const val ONBOARD_START_BUTTON = "Onboarding_Start_Button"
private const val ONBOARD_CREATE_PHONE_SCREEN = "Creation_Phone_Screen"
private const val ONBOARD_CREATE_PHONE_CLICK_BUTTON = "Create_Phone_Click_Button"
private const val ONBOARD_CREATE_SMS_SCREEN = "Create_Sms_Screen"
private const val ONBOARD_CREATE_SMS_VALIDATION = "Create_Sms_Validation"
private const val ONBOARD_CREATE_WALLET_CONFIRM_PIN = "Create_Confirm_Pin"

class CreateWalletAnalytics(
    private val tracker: Analytics
) {

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

    fun logCreatePhoneEnterScreenOpened() {
        tracker.logEvent(ONBOARD_CREATE_PHONE_SCREEN)
    }

    fun logCreateConfirmPhoneButtonClicked() {
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
}

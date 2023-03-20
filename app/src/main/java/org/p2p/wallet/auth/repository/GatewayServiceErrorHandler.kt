package org.p2p.wallet.auth.repository

import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.model.PushServiceError
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.ButtonAction
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.GoogleButton
import org.p2p.wallet.auth.model.PrimaryFirstButton
import org.p2p.wallet.auth.model.SecondaryFirstButton
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.common.ResourcesProvider

private const val DEFAULT_BLOCK_TIME_IN_MINUTES = 10

class GatewayServiceErrorHandler(
    private val resourcesProvider: ResourcesProvider,
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor
) {

    fun handle(error: PushServiceError): GatewayHandledState? {
        return when (error) {
            is PushServiceError.CriticalServiceFailure -> {
                GatewayHandledState.CriticalError(error.code)
            }
            is PushServiceError.IncorrectOtpCode -> {
                GatewayHandledState.IncorrectOtpCodeError
            }
            is PushServiceError.PhoneNumberAlreadyConfirmed -> {
                GatewayHandledState.ToastError(
                    resourcesProvider.getString(
                        R.string.onboarding_phone_enter_error_phone_confirmed
                    )
                )
            }
            is PushServiceError.PhoneNumberNotExists -> {
                val isDeviceShareSaved = restoreWalletInteractor.isDeviceShareSaved()
                val userPhoneNumber = onboardingInteractor.temporaryPhoneNumber?.formattedValue.orEmpty()

                val title = resourcesProvider.getString(
                    if (isDeviceShareSaved) {
                        R.string.restore_no_wallet_title
                    } else {
                        R.string.restore_no_account_title
                    }
                )
                val subtitle = resourcesProvider.getString(
                    if (isDeviceShareSaved) {
                        R.string.restore_no_wallet_try_another_option
                    } else {
                        R.string.restore_no_wallet_found_with_no_device_share_message
                    },
                    userPhoneNumber
                )
                val firstButtonTitleResId =
                    R.string.restore_continue_with_google.takeIf { isDeviceShareSaved }
                        ?: R.string.restore_another_phone_number

                val firstButtonIcon = if (isDeviceShareSaved) R.drawable.ic_google_logo else null
                val buttonAction =
                    ButtonAction.NAVIGATE_GOOGLE_AUTH.takeIf { isDeviceShareSaved }
                        ?: ButtonAction.NAVIGATE_ENTER_PHONE

                val primaryFirstButton = PrimaryFirstButton(
                    titleResId = R.string.restore_phone_number,
                    buttonAction = ButtonAction.NAVIGATE_ENTER_PHONE
                )
                    .takeIf { isDeviceShareSaved }

                val secondaryFirstButton = SecondaryFirstButton(
                    titleResId = R.string.onboarding_continue_starting_button_text,
                    buttonAction = ButtonAction.NAVIGATE_START_SCREEN
                )

                GatewayHandledState.TitleSubtitleError(
                    title = title,
                    subtitle = subtitle,
                    googleButton = GoogleButton(
                        titleResId = firstButtonTitleResId,
                        iconResId = firstButtonIcon,
                        buttonAction = buttonAction
                    ),
                    primaryFirstButton = primaryFirstButton,
                    secondaryFirstButton = secondaryFirstButton
                )
            }
            is PushServiceError.SmsDeliverFailed -> {
                GatewayHandledState.ToastError(
                    resourcesProvider.getString(R.string.onboarding_phone_enter_error_sms_failed)
                )
            }

            is PushServiceError.TooManyOtpRequests -> {
                val cooldownTtl = error.cooldownTtl
                val message = resourcesProvider.getString(R.string.error_too_often_otp_requests_message)
                GatewayHandledState.ToastError(message)
            }
            is PushServiceError.TooManyRequests -> {
                val cooldownTtl = error.cooldownTtl
                val error = GeneralErrorTimerScreenError.BLOCK_SMS_TOO_MANY_WRONG_ATTEMPTS
                GatewayHandledState.TimerBlockError(error = error, cooldownTtl = cooldownTtl)
            }
            is PushServiceError.UserAlreadyExists -> {
                val message = resourcesProvider.getString(R.string.onboarding_phone_enter_error_phone_confirmed)
                GatewayHandledState.ToastError(message)
            }
            else -> {
                null
            }
        }
    }
}

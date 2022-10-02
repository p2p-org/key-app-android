package org.p2p.wallet.auth.repository

import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ButtonAction
import org.p2p.wallet.auth.model.GoogleButton
import org.p2p.wallet.auth.model.PrimaryFirstButton
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.model.RestoreHandledState
import org.p2p.wallet.auth.model.RestoreSuccessState
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.model.SecondaryFirstButton
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.utils.orZero

class RestoreUserExceptionHandler(
    private val resourcesProvider: ResourcesProvider,
) {

    fun handleRestoreResult(result: RestoreUserResult): RestoreHandledState =
        when (result) {
            is RestoreUserResult.RestoreFailure -> handleRestoreFailure(result)
            is RestoreUserResult.RestoreSuccess -> RestoreSuccessState()
        }

    private fun handleRestoreFailure(result: RestoreUserResult.RestoreFailure): RestoreHandledState =
        when (result) {
            is RestoreUserResult.RestoreFailure.SocialPlusCustomShare -> {
                handleResult(result)
            }
            is RestoreUserResult.RestoreFailure.DevicePlusSocialShare -> {
                handleResult(result)
            }
            is RestoreUserResult.RestoreFailure.DevicePlusCustomShare -> {
                handleResult(result)
            }
            else -> error("Cannot handle unknown state")
        }

    private fun handleResult(result: RestoreUserResult.RestoreFailure.SocialPlusCustomShare): RestoreHandledState =
        when (result) {
            is RestoreUserResult.RestoreFailure.SocialPlusCustomShare -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.onboarding_general_error_critical_error_title),
                    subtitle = resourcesProvider.getString(
                        R.string.onboarding_general_error_critical_error_sub_title,
                        result.exception.errorCode.orZero()
                    ),
                    googleButton = GoogleButton(
                        titleResId = R.string.onboarding_general_error_bug_report_button_title,
                        iconResId = R.drawable.ic_caution,
                        iconTintResId = R.color.icons_night,
                        buttonAction = ButtonAction.OPEN_INTERCOM,
                        isVisible = true
                    ),
                    primaryFirstButton = PrimaryFirstButton(),
                    secondaryFirstButton = SecondaryFirstButton(
                        titleResId = R.string.onboarding_general_error_starting_screen_button_title,
                        buttonAction = ButtonAction.NAVIGATE_START_SCREEN,
                        isVisible = true
                    )
                )
            }
            else -> RestoreFailureState.ToastError("Error on restore Social + Custom Share")
        }

    private fun handleResult(result: RestoreUserResult.RestoreFailure.DevicePlusSocialShare): RestoreHandledState =
        when (result) {
            is RestoreUserResult.RestoreFailure.DevicePlusSocialShare.SocialShareNotFound -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.restore_no_wallet_title),
                    email = resourcesProvider.getString(R.string.onboarding_with_email, result.userEmailAddress),
                    subtitle = resourcesProvider.getString(R.string.restore_no_wallet_found_with_device_share_message),
                    googleButton = GoogleButton(
                        buttonAction = ButtonAction.NAVIGATE_GOOGLE_AUTH
                    ),
                    primaryFirstButton = PrimaryFirstButton(
                        buttonAction = ButtonAction.NAVIGATE_ENTER_PHONE
                    ),
                    secondaryFirstButton = SecondaryFirstButton(
                        buttonAction = ButtonAction.NAVIGATE_START_SCREEN
                    )
                )
            }
            is RestoreUserResult.RestoreFailure.DevicePlusSocialShare.DeviceAndSocialShareNotMatch -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.auth_almost_done_title),
                    email = resourcesProvider.getString(R.string.onboarding_with_email, result.userEmailAddress),
                    subtitle = resourcesProvider.getString(R.string.restore_no_wallet_found_with_device_share_message),
                    googleButton = GoogleButton(
                        buttonAction = ButtonAction.NAVIGATE_GOOGLE_AUTH,
                    ),
                    primaryFirstButton = PrimaryFirstButton(
                        buttonAction = ButtonAction.NAVIGATE_ENTER_PHONE
                    ),
                    secondaryFirstButton = SecondaryFirstButton(
                        buttonAction = ButtonAction.NAVIGATE_START_SCREEN
                    )
                )
            }
            else -> error("Unknown restore error state")
        }

    private fun handleResult(result: RestoreUserResult.RestoreFailure.DevicePlusCustomShare): RestoreHandledState =
        when (result) {
            is RestoreUserResult.RestoreFailure.DevicePlusCustomShare.SharesDoesNotMatch -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.error_shares_do_not_matches_title),
                    subtitle = resourcesProvider.getString(R.string.error_shares_do_not_matches_message),
                    googleButton = GoogleButton(
                        titleResId = R.string.onboarding_general_error_bug_report_button_title,
                        iconResId = R.drawable.ic_caution,
                        iconTintResId = R.color.icons_night,
                        buttonAction = ButtonAction.OPEN_INTERCOM
                    ),
                    secondaryFirstButton = SecondaryFirstButton(
                        buttonAction = ButtonAction.NAVIGATE_START_SCREEN
                    )
                )
            }
            is RestoreUserResult.RestoreFailure.DevicePlusCustomShare.UserNotFound -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.error_wallet_not_found_title),
                    subtitle = resourcesProvider.getString(R.string.error_wallet_not_found_message),
                    googleButton = GoogleButton(
                        buttonAction = ButtonAction.NAVIGATE_GOOGLE_AUTH
                    ),
                    primaryFirstButton = PrimaryFirstButton(
                        buttonAction = ButtonAction.NAVIGATE_ENTER_PHONE
                    ),
                    secondaryFirstButton = SecondaryFirstButton(
                        buttonAction = ButtonAction.NAVIGATE_START_SCREEN
                    )
                )
            }
            else -> error("Unknown error case")
        }
}

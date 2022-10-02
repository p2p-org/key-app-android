package org.p2p.wallet.auth.repository

import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.RestoreStateMachine
import org.p2p.wallet.auth.model.ButtonAction
import org.p2p.wallet.auth.model.GoogleButton
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.PrimaryFirstButton
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.model.RestoreHandledState
import org.p2p.wallet.auth.model.RestoreSuccessState
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.model.SecondaryFirstButton
import org.p2p.wallet.common.ResourcesProvider

class RestoreUserExceptionHandler(
    private val resourcesProvider: ResourcesProvider,
    private val restoreStateMachine: RestoreStateMachine,
    private val onboardingInteractor: OnboardingInteractor
) {

    fun handleRestoreResult(result: RestoreUserResult): RestoreHandledState =
        when (result) {
            is RestoreUserResult.RestoreFailure -> handleRestoreFailure(result)
            is RestoreUserResult.RestoreSuccess -> RestoreSuccessState()
        }

    private fun onSharesNotMatch() = RestoreFailureState.TitleSubtitleError(
        title = resourcesProvider.getString(R.string.restore_shares_not_match_title),
        subtitle = resourcesProvider.getString(R.string.restore_shares_not_match_message),
        googleButton = GoogleButton(
            titleResId = R.string.onboarding_general_error_bug_report_button_title,
            iconResId = R.drawable.ic_caution,
            iconTintResId = R.color.icons_night,
            buttonAction = ButtonAction.OPEN_INTERCOM,
            isVisible = true
        ),
        secondaryFirstButton = SecondaryFirstButton(
            titleResId = R.string.onboarding_general_error_starting_screen_button_title,
            buttonAction = ButtonAction.NAVIGATE_START_SCREEN,
            isVisible = true
        )
    )

    private fun handleRestoreFailure(result: RestoreUserResult.RestoreFailure): RestoreHandledState {
        val handledResult = when (result) {
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
        // Temporary solution for local error handling
        if (result !is RestoreUserResult.RestoreFailure.SocialPlusCustomShare.TorusKeyNotFound) {
            restoreStateMachine.onRestoreFailure(onboardingInteractor.currentFlow as OnboardingFlow.RestoreWallet)
        }
        if (!restoreStateMachine.isRestoreAvailable()) {
            return onSharesNotMatch()
        }
        return handledResult
    }

    private fun handleResult(result: RestoreUserResult.RestoreFailure.SocialPlusCustomShare): RestoreHandledState {
        return when (result) {
            is RestoreUserResult.RestoreFailure.SocialPlusCustomShare.TorusKeyNotFound -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.restore_how_to_continue),
                    subtitle = "",
                    googleButton = GoogleButton(
                        buttonAction = ButtonAction.NAVIGATE_GOOGLE_AUTH,
                        isVisible = true
                    ),
                    imageViewResId = R.drawable.easy_to_start
                )
            }
            else -> RestoreFailureState.ToastError("Error on restore Social + Custom Share")
        }
    }

    private fun handleResult(result: RestoreUserResult.RestoreFailure.DevicePlusSocialShare): RestoreHandledState {
        return when (result) {
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

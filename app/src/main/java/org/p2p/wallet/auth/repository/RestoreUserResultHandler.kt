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
import org.p2p.wallet.auth.statemachine.RestoreState
import org.p2p.wallet.auth.statemachine.RestoreStateMachine
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.utils.emptyString
import timber.log.Timber

class RestoreUserResultHandler(
    private val resourcesProvider: ResourcesProvider,
    private val restoreStateMachine: RestoreStateMachine
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
            else -> {
                Timber.i(result)
                error("Unknown restore error state for RestoreFailure: $result")
            }
        }

    private fun handleResult(result: RestoreUserResult.RestoreFailure.SocialPlusCustomShare): RestoreHandledState {
        return when (result) {
            is RestoreUserResult.RestoreFailure.SocialPlusCustomShare.TorusKeyNotFound -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.restore_how_to_continue),
                    subtitle = emptyString(),
                    googleButton = GoogleButton(
                        buttonAction = ButtonAction.NAVIGATE_GOOGLE_AUTH,
                        isVisible = true
                    ),
                    imageViewResId = R.drawable.easy_to_start
                )
            }
            is RestoreUserResult.RestoreFailure.SocialPlusCustomShare.SocialShareNotFound -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.restore_no_wallet_title),
                    email = resourcesProvider.getString(R.string.onboarding_with_email, result.userEmailAddress),
                    subtitle = resourcesProvider.getString(R.string.error_shares_do_not_matches_message),
                    googleButton = GoogleButton(
                        buttonAction = ButtonAction.NAVIGATE_GOOGLE_AUTH
                    ),
                    secondaryFirstButton = SecondaryFirstButton(
                        buttonAction = ButtonAction.NAVIGATE_START_SCREEN
                    )
                )
            }
            is RestoreUserResult.RestoreFailure.SocialPlusCustomShare.SocialShareNotMatch -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.auth_almost_done_title),
                    email = resourcesProvider.getString(R.string.onboarding_with_email, result.userEmailAddress),
                    subtitle = resourcesProvider.getString(R.string.error_shares_do_not_matches_message),
                    googleButton = GoogleButton(
                        buttonAction = ButtonAction.NAVIGATE_GOOGLE_AUTH
                    ),
                    secondaryFirstButton = SecondaryFirstButton(
                        buttonAction = ButtonAction.NAVIGATE_START_SCREEN
                    )
                )
            }
            else -> {
                Timber.i(result.exception)
                RestoreFailureState.ToastError("Error on restore Social + Custom Share")
            }
        }
    }

    private fun handleResult(result: RestoreUserResult.RestoreFailure.DevicePlusSocialShare): RestoreHandledState {
        return when (result) {
            is RestoreUserResult.RestoreFailure.DevicePlusSocialShare.SocialShareNotFound -> {
                restoreStateMachine.updateState(RestoreState.DeviceSocialShareNotFoundState())
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
                restoreStateMachine.updateState(RestoreState.DevicePlusSocialShareNotMatchState())
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
            else -> {
                Timber.i(result.exception)
                error("Unknown restore error state for Device+Social: $result")
            }
        }
    }

    private fun handleResult(result: RestoreUserResult.RestoreFailure.DevicePlusCustomShare): RestoreHandledState =
        when (result) {
            is RestoreUserResult.RestoreFailure.DevicePlusCustomShare.SharesDoesNotMatch -> {
                restoreStateMachine.updateState(RestoreState.DevicePlusCustomShareNotMatchState())
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
                restoreStateMachine.updateState(RestoreState.DeviceCustomShareNotFound())
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
            else -> {
                Timber.i(result.exception)
                error("Unknown restore error state for Device+Custom: $result")
            }
        }
}

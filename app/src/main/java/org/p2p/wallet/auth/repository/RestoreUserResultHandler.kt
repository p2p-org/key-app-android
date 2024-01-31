package org.p2p.wallet.auth.repository

import android.content.res.Resources
import timber.log.Timber
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.ButtonAction
import org.p2p.wallet.auth.model.GoogleButton
import org.p2p.wallet.auth.model.PrimaryFirstButton
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.model.RestoreHandledState
import org.p2p.wallet.auth.model.RestoreSuccessState
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.model.RestoreUserResult.RestoreFailure
import org.p2p.wallet.auth.model.SecondaryFirstButton
import org.p2p.wallet.auth.statemachine.RestoreState
import org.p2p.wallet.auth.statemachine.RestoreStateMachine
import org.p2p.wallet.utils.emptyString

private const val TAG = "RestoreUserResultHandler"
class RestoreUserResultHandler(
    private val resources: Resources,
    private val restoreStateMachine: RestoreStateMachine
) {

    fun handleRestoreResult(result: RestoreUserResult): RestoreHandledState =
        when (result) {
            is RestoreFailure -> handleRestoreFailure(result)
            is RestoreUserResult.RestoreSuccess -> RestoreSuccessState()
        }

    private fun handleRestoreFailure(result: RestoreFailure): RestoreHandledState {
        Timber.tag(TAG).i("Restore failed for ${result.javaClass.simpleName}")
        return when (result) {
            is RestoreFailure.SocialPlusCustomShare -> {
                handleResult(result)
            }
            is RestoreFailure.DevicePlusSocialShare -> {
                handleResult(result)
            }
            is RestoreFailure.DevicePlusCustomShare -> {
                handleResult(result)
            }
            is RestoreFailure.DevicePlusSocialOrSocialPlusCustom -> {
                handleShareAreNotMatchResult()
            }
            is RestoreFailure.DevicePlusCustomOrSocialPlusCustom -> {
                handleShareAreNotMatchResult()
            }
            else -> {
                Timber.i(result)
                error("Unknown restore error state for RestoreFailure: $result")
            }
        }
    }

    private fun handleShareAreNotMatchResult(): RestoreHandledState {
        return RestoreFailureState.TitleSubtitleError(
            title = resources.getString(R.string.error_wallet_not_found_title),
            subtitle = resources.getString(R.string.error_shares_do_not_matches_message),
            googleButton = null,
            secondaryFirstButton = SecondaryFirstButton(
                titleResId = R.string.restore_starting_screen,
                buttonAction = ButtonAction.NAVIGATE_START_SCREEN
            ),
            imageViewResId = R.drawable.ic_cat
        )
    }

    private fun handleResult(result: RestoreFailure.SocialPlusCustomShare): RestoreHandledState {
        return when (result) {
            is RestoreFailure.SocialPlusCustomShare.TorusKeyNotFound -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resources.getString(R.string.restore_how_to_continue),
                    subtitle = emptyString(),
                    googleButton = GoogleButton(
                        buttonAction = ButtonAction.NAVIGATE_GOOGLE_AUTH,
                        isVisible = true
                    ),
                    imageViewResId = R.drawable.easy_to_start
                )
            }
            is RestoreFailure.SocialPlusCustomShare.SocialShareNotFound -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resources.getString(R.string.restore_no_wallet_title),
                    email = resources.getString(R.string.onboarding_with_email, result.userEmailAddress),
                    subtitle = resources.getString(R.string.error_shares_do_not_matches_message),
                    googleButton = GoogleButton(
                        buttonAction = ButtonAction.NAVIGATE_GOOGLE_AUTH
                    ),
                    secondaryFirstButton = SecondaryFirstButton(
                        buttonAction = ButtonAction.NAVIGATE_START_SCREEN
                    )
                )
            }
            is RestoreFailure.SocialPlusCustomShare.SocialShareNotMatch -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resources.getString(R.string.restore_no_wallet_title),
                    email = resources.getString(R.string.onboarding_with_email, result.userEmailAddress),
                    subtitle = resources.getString(R.string.restore_no_wallet_try_another_option),
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
                RestoreFailureState.LogError(result.message ?: result.exception.message.orEmpty())
            }
        }
    }

    private fun handleResult(result: RestoreFailure.DevicePlusSocialShare): RestoreHandledState {
        return when (result) {
            is RestoreFailure.DevicePlusSocialShare.SocialShareNotFound -> {
                restoreStateMachine.updateState(RestoreState.DeviceSocialShareNotFoundState())
                RestoreFailureState.TitleSubtitleError(
                    title = resources.getString(R.string.restore_no_wallet_title),
                    email = resources.getString(R.string.onboarding_with_email, result.userEmailAddress),
                    subtitle = resources.getString(R.string.restore_no_wallet_try_another_option),
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
            is RestoreFailure.DevicePlusSocialShare.DeviceAndSocialShareNotMatch -> {
                restoreStateMachine.updateState(RestoreState.DevicePlusSocialShareNotMatchState())

                RestoreFailureState.TitleSubtitleError(
                    title = resources.getString(R.string.auth_almost_done_title),
                    email = resources.getString(
                        R.string.onboarding_if_you_want_continue_with_email,
                        result.userEmailAddress
                    ),
                    subtitle = resources.getString(R.string.restore_select_phone_number_if_you_made_a_mistake),
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
                RestoreFailureState.LogError(result.message ?: result.exception.message.orEmpty())
            }
        }
    }

    private fun handleResult(result: RestoreFailure.DevicePlusCustomShare): RestoreHandledState =
        when (result) {
            is RestoreFailure.DevicePlusCustomShare.SharesDoesNotMatch -> {
                restoreStateMachine.updateState(RestoreState.DevicePlusCustomShareNotMatchState())
                RestoreFailureState.TitleSubtitleError(
                    title = resources.getString(R.string.auth_almost_done_title),
                    subtitle = resources.getString(R.string.restore_use_your_social_account),
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
            is RestoreFailure.DevicePlusCustomShare.UserNotFound -> {
                restoreStateMachine.updateState(RestoreState.DeviceCustomShareNotFound())
                RestoreFailureState.TitleSubtitleError(
                    title = resources.getString(R.string.error_wallet_not_found_title),
                    subtitle = resources.getString(R.string.restore_no_wallet_try_another_option),
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
                RestoreFailureState.LogError(result.message ?: result.exception.message.orEmpty())
            }
        }
}

package org.p2p.wallet.auth.repository

import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.RestoreUserResult
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
                    googleButton = RestoreFailureState.TitleSubtitleError.GoogleButton(
                        titleResId = R.string.onboarding_general_error_bug_report_button_title,
                        iconResId = R.drawable.ic_caution,
                        iconTintResId = R.color.icons_night,
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.OPEN_INTERCOM,
                        isVisible = true
                    ),
                    primaryFirstButton = RestoreFailureState.TitleSubtitleError.PrimaryFirstButton(),
                    secondaryFirstButton = RestoreFailureState.TitleSubtitleError.SecondaryFirstButton(
                        titleResId = R.string.onboarding_general_error_starting_screen_button_title,
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_START_SCREEN,
                        isVisible = true
                    )
                )
            }
            else -> error("Unknown restore error state")
        }

    private fun handleResult(result: RestoreUserResult.RestoreFailure.DevicePlusSocialShare): RestoreHandledState =
        when (result) {
            is RestoreUserResult.RestoreFailure.DevicePlusSocialShare.SocialShareNotFound -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.restore_no_wallet_title),
                    email = resourcesProvider.getString(R.string.onboarding_with_email, result.userEmailAddress),
                    subtitle = resourcesProvider.getString(R.string.restore_no_wallet_found_with_device_share_message),
                    googleButton = RestoreFailureState.TitleSubtitleError.GoogleButton(
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_GOOGLE_AUTH
                    ),
                    primaryFirstButton = RestoreFailureState.TitleSubtitleError.PrimaryFirstButton(
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_ENTER_PHONE
                    ),
                    secondaryFirstButton = RestoreFailureState.TitleSubtitleError.SecondaryFirstButton(
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_START_SCREEN
                    )
                )
            }
            is RestoreUserResult.RestoreFailure.DevicePlusSocialShare.DeviceAndSocialShareNotMatch -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.auth_almost_done_title),
                    email = resourcesProvider.getString(R.string.onboarding_with_email, result.userEmailAddress),
                    subtitle = resourcesProvider.getString(R.string.restore_no_wallet_found_with_device_share_message),
                    googleButton = RestoreFailureState.TitleSubtitleError.GoogleButton(
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_GOOGLE_AUTH,
                    ),
                    primaryFirstButton = RestoreFailureState.TitleSubtitleError.PrimaryFirstButton(
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_ENTER_PHONE
                    ),
                    secondaryFirstButton = RestoreFailureState.TitleSubtitleError.SecondaryFirstButton(
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_START_SCREEN
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
                    googleButton = RestoreFailureState.TitleSubtitleError.GoogleButton(
                        titleResId = R.string.onboarding_general_error_bug_report_button_title,
                        iconResId = R.drawable.ic_caution,
                        iconTintResId = R.color.icons_night,
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.OPEN_INTERCOM
                    ),
                    secondaryFirstButton = RestoreFailureState.TitleSubtitleError.SecondaryFirstButton(
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_START_SCREEN
                    )
                )
            }
            is RestoreUserResult.RestoreFailure.DevicePlusCustomShare.UserNotFound -> {
                RestoreFailureState.TitleSubtitleError(
                    title = resourcesProvider.getString(R.string.error_wallet_not_found_title),
                    subtitle = resourcesProvider.getString(R.string.error_wallet_not_found_message),
                    googleButton = RestoreFailureState.TitleSubtitleError.GoogleButton(
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_GOOGLE_AUTH
                    ),
                    primaryFirstButton = RestoreFailureState.TitleSubtitleError.PrimaryFirstButton(
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_ENTER_PHONE
                    ),
                    secondaryFirstButton = RestoreFailureState.TitleSubtitleError.SecondaryFirstButton(
                        buttonAction = RestoreFailureState.TitleSubtitleError.ButtonAction.NAVIGATE_START_SCREEN
                    )
                )
            }
            else -> error("Unknown error case")
        }
}

abstract class RestoreHandledState
class RestoreSuccessState : RestoreHandledState()

abstract class RestoreFailureState(
    open val googleButton: TitleSubtitleError.GoogleButton? = null,
    open val primaryFirstButton: TitleSubtitleError.PrimaryFirstButton? = null,
    open val secondaryFirstButton: TitleSubtitleError.SecondaryFirstButton? = null
) : RestoreHandledState(), Parcelable {

    @Parcelize
    data class TitleSubtitleError(
        val title: String,
        val subtitle: String,
        val email: String? = null,
        override val googleButton: GoogleButton? = null,
        override val primaryFirstButton: PrimaryFirstButton? = null,
        override val secondaryFirstButton: SecondaryFirstButton? = null
    ) : RestoreFailureState(
        googleButton = GoogleButton(),
        primaryFirstButton = PrimaryFirstButton(),
        secondaryFirstButton = SecondaryFirstButton()
    ) {
        @Parcelize
        data class GoogleButton(
            @StringRes val titleResId: Int = R.string.restore_continue_with_google,
            @DrawableRes val iconResId: Int? = null,
            @ColorRes val iconTintResId: Int? = null,
            val buttonAction: ButtonAction = ButtonAction.NAVIGATE_GOOGLE_AUTH,
            val isVisible: Boolean = false
        ) : Parcelable

        @Parcelize
        data class PrimaryFirstButton(
            @StringRes val titleResId: Int = R.string.restore_phone_number,
            val isVisible: Boolean = false,
            val buttonAction: ButtonAction = ButtonAction.NAVIGATE_ENTER_PHONE
        ) : Parcelable

        @Parcelize
        data class SecondaryFirstButton(
            @StringRes val titleResId: Int = R.string.restore_starting_screen,
            val buttonAction: ButtonAction = ButtonAction.NAVIGATE_START_SCREEN,
            val isVisible: Boolean = false
        ) : Parcelable

        enum class ButtonAction {
            OPEN_INTERCOM,
            NAVIGATE_GOOGLE_AUTH,
            NAVIGATE_ENTER_PHONE,
            NAVIGATE_START_SCREEN
        }
    }
}

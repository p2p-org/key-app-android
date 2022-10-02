package org.p2p.wallet.auth.model

import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.R

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

package org.p2p.wallet.settings.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.R

sealed class SettingsRow(open val isDivider: Boolean = false) {
    abstract val titleResId: Int

    data class Section(
        @StringRes override val titleResId: Int,
        override val isDivider: Boolean = false,
        @StringRes val subtitleRes: Int? = null,
        @DrawableRes val iconRes: Int,
        val subtitle: String? = null,
        @ColorRes val subtitleTextColorRes: Int? = null,
    ) : SettingsRow(isDivider)

    data class Title(
        @StringRes override val titleResId: Int,
        override val isDivider: Boolean = false
    ) : SettingsRow(isDivider)

    data class Logout(
        override val titleResId: Int = R.string.settings_logout
    ) : SettingsRow(isDivider = true)

    data class Info(
        @StringRes override val titleResId: Int,
        override val isDivider: Boolean = false,
        val subtitle: String? = null,
    ) : SettingsRow(isDivider)

    data class Switcher(
        override val titleResId: Int,
        override val isDivider: Boolean,
        @DrawableRes val iconRes: Int,
        val subtitle: String? = null,
        val isSelected: Boolean = false
    ) : SettingsRow(isDivider)

    data class PopupMenu(
        override val titleResId: Int,
        val selectedItem: String,
        val menuOptions: List<String>
    ) : SettingsRow(false)
}

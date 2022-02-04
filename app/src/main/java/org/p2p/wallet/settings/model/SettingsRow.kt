package org.p2p.wallet.settings.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class SettingsRow(open val isDivider: Boolean = false) {

    data class Section(
        override val isDivider: Boolean = false,
        @StringRes val titleRes: Int,
        @StringRes val subtitleRes: Int = -1,
        @DrawableRes val iconRes: Int,
        val subtitle: String? = null
    ) : SettingsRow(isDivider)

    data class Title(
        @StringRes val titleResId: Int,
        override val isDivider: Boolean = false
    ) : SettingsRow(isDivider)

    object Logout : SettingsRow(isDivider = true)
}
package org.p2p.wallet.settings.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed interface SettingsItem {
    data class SettingsGroupTitleItem(
        @StringRes val groupTitleRes: Int
    ) : SettingsItem

    data class TextSettingsItem(
        @StringRes val settingNameRes: Int,
        @DrawableRes val iconRes: Int,
        val textValue: String,
        val hasSeparator: Boolean
    ) : SettingsItem

    object SignOutButtonItem : SettingsItem

    data class SwitchSettingsItem(
        @StringRes val nameRes: Int,
        @DrawableRes val iconRes: Int,
        val isSwitched: Boolean,
        val hasSeparator: Boolean
    ) : SettingsItem

    data class ComplexSettingsItem(
        @StringRes val nameRes: Int,
        @DrawableRes val iconRes: Int,
        val additionalText: String? = null,
        val hasSeparator: Boolean,
        val isBadgeVisible: Boolean = false
    ) : SettingsItem

    object SettingsSpaceSeparatorItem : SettingsItem
}

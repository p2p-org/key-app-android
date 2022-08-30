package org.p2p.wallet.settings.ui.settings.presenter

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed interface SettingsItem {
    data class SettingsGroupTitleItem(
        @StringRes val groupTitleRes: Int
    ) : SettingsItem

    data class TextSettingItem(
        @StringRes val settingNameRes: Int,
        @DrawableRes val iconRes: Int,
        val textValue: String,
        val hasSeparator: Boolean
    ) : SettingsItem

    object SignOutButtonItem : SettingsItem

    data class SwitchSettingItem(
        @StringRes val settingNameRes: Int,
        @DrawableRes val iconRes: Int,
        val isSwitched: Boolean,
        val hasSeparator: Boolean
    ) : SettingsItem

    data class ComplexSettingItem(
        @StringRes val settingNameRes: Int,
        @DrawableRes val iconRes: Int,
        val additionalText: String? = null,
        val hasSeparator: Boolean
    ) : SettingsItem

    object SettingsSpaceSeparatorItem : SettingsItem
}

package org.p2p.wallet.settings.ui.settings.presenter

import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.ComplexSettingItem
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.SettingsGroupTitleItem
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.SettingsSpaceSeparatorItem
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.SignOutButtonItem
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.SwitchSettingItem
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.TextSettingItem
import timber.log.Timber

class SettingsItemMapper(
    private val resourcesProvider: ResourcesProvider
) {
    fun createItems(
        username: Username?,
        isBiometricConfirmEnabled: Boolean,
        isZeroBalanceTokenHidden: Boolean
    ): List<SettingsItem> = buildList {
        Timber.i(isZeroBalanceTokenHidden.toString())
        this += profileBlock(username)
        this += securityBlock(isBiometricConfirmEnabled)
        this += appearanceBlock(isZeroBalanceTokenHidden)
        this += appInfoBlock()
    }

    private fun profileBlock(username: Username?): List<SettingsItem> = listOf(
        SettingsSpaceSeparatorItem,
        SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_profile),
        ComplexSettingItem(
            settingNameRes = R.string.settings_item_title_username,
            iconRes = R.drawable.ic_settings_user,
            additionalText = username?.getFullUsername(resourcesProvider)
                ?: resourcesProvider.getString(R.string.settings_item_username_not_reserved),
            hasSeparator = false
        ),
        SignOutButtonItem,
        SettingsSpaceSeparatorItem
    )

    private fun securityBlock(isBiometricAuthEnabled: Boolean): List<SettingsItem> = listOf(
        SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_security),
        ComplexSettingItem(
            settingNameRes = R.string.settings_item_title_pin,
            iconRes = R.drawable.ic_settings_pin,
            hasSeparator = true
        ),
        ComplexSettingItem(
            settingNameRes = R.string.settings_item_title_networks,
            iconRes = R.drawable.ic_settings_network,
            hasSeparator = true
        ),
        SwitchSettingItem(
            settingNameRes = R.string.settings_item_title_touch_id,
            iconRes = R.drawable.ic_settings_fingerprint,
            isSwitched = isBiometricAuthEnabled,
            hasSeparator = false
        ),
        SettingsSpaceSeparatorItem,
    )

    private fun appearanceBlock(isZeroBalanceTokenHidden: Boolean): List<SettingsItem> {
        return listOf(
            SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_appearance),
            SwitchSettingItem(
                settingNameRes = R.string.settings_item_title_zero_balances,
                iconRes = R.drawable.ic_settings_hidden_eye,
                isSwitched = isZeroBalanceTokenHidden,
                hasSeparator = false
            ),
            SettingsSpaceSeparatorItem,
        )
    }

    private fun appInfoBlock(): List<SettingsItem> {
        return listOf(
            TextSettingItem(
                settingNameRes = R.string.settings_app_version,
                iconRes = R.drawable.ic_settings_phone,
                textValue = BuildConfig.VERSION_NAME,
                hasSeparator = false
            )
        )
    }
}

package org.p2p.wallet.settings.model

import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.settings.model.SettingsItem.ComplexSettingsItem
import org.p2p.wallet.settings.model.SettingsItem.SettingsGroupTitleItem
import org.p2p.wallet.settings.model.SettingsItem.SettingsSpaceSeparatorItem
import org.p2p.wallet.settings.model.SettingsItem.SignOutButtonItem
import org.p2p.wallet.settings.model.SettingsItem.SwitchSettingsItem
import org.p2p.wallet.settings.model.SettingsItem.TextSettingsItem
import timber.log.Timber

class SettingsItemMapper(
    private val resourcesProvider: ResourcesProvider
) {
    fun createItems(
        username: Username?,
        isBiometricLoginEnabled: Boolean,
        isZeroBalanceTokenHidden: Boolean,
        isBiometricLoginAvailable: Boolean
    ): List<SettingsItem> = buildList {
        Timber.i(isZeroBalanceTokenHidden.toString())
        this += profileBlock(username)
        this += securityBlock(isBiometricLoginEnabled, isBiometricLoginAvailable)
        this += appearanceBlock(isZeroBalanceTokenHidden)
        this += appInfoBlock()
    }

    private fun profileBlock(username: Username?): List<SettingsItem> = listOf(
        SettingsSpaceSeparatorItem,
        SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_profile),
        ComplexSettingsItem(
            nameRes = R.string.settings_item_title_username,
            iconRes = R.drawable.ic_settings_user,
            additionalText = username?.fullUsername
                ?: resourcesProvider.getString(R.string.settings_item_username_not_reserved),
            hasSeparator = false
        ),
        ComplexSettingsItem(
            nameRes = R.string.settings_item_title_support,
            iconRes = R.drawable.ic_settings_support,
            hasSeparator = false
        ),
        SignOutButtonItem,
        SettingsSpaceSeparatorItem
    )

    private fun securityBlock(
        isBiometricLoginEnabled: Boolean,
        isBiometricLoginAvailable: Boolean
    ): List<SettingsItem> = listOfNotNull(
        SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_security),
        ComplexSettingsItem(
            nameRes = R.string.settings_item_title_pin,
            iconRes = R.drawable.ic_settings_pin,
            hasSeparator = true
        ),
        ComplexSettingsItem(
            nameRes = R.string.settings_item_title_networks,
            iconRes = R.drawable.ic_settings_network,
            hasSeparator = true
        ),
        SwitchSettingsItem(
            nameRes = R.string.settings_item_title_touch_id,
            iconRes = R.drawable.ic_settings_fingerprint,
            isSwitched = isBiometricLoginEnabled,
            hasSeparator = false
        ).takeIf { isBiometricLoginAvailable },
        SettingsSpaceSeparatorItem,
    )

    private fun appearanceBlock(isZeroBalanceTokenHidden: Boolean): List<SettingsItem> {
        return listOf(
            SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_appearance),
            SwitchSettingsItem(
                nameRes = R.string.settings_item_title_zero_balances,
                iconRes = R.drawable.ic_settings_hidden_eye,
                isSwitched = isZeroBalanceTokenHidden,
                hasSeparator = false
            ),
            SettingsSpaceSeparatorItem,
        )
    }

    private fun appInfoBlock(): List<SettingsItem> {
        return listOf(
            TextSettingsItem(
                settingNameRes = R.string.settings_app_version,
                iconRes = R.drawable.ic_settings_phone,
                textValue = BuildConfig.VERSION_NAME,
                hasSeparator = false
            )
        )
    }
}

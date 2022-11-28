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

class SettingsItemMapper(
    private val resourcesProvider: ResourcesProvider
) {
    fun createItems(
        username: Username?,
        isUsernameItemVisible: Boolean,
        isBiometricLoginEnabled: Boolean,
        isZeroBalanceTokenHidden: Boolean,
        isBiometricLoginAvailable: Boolean,
        isRecoveryKitAvailable: Boolean
    ): List<SettingsItem> = buildList {
        this += profileBlock(username, isUsernameItemVisible)
        this += securityBlock(
            isRecoveryKitAvailable,
            isBiometricLoginEnabled,
            isBiometricLoginAvailable
        )
        this += appearanceBlock(isZeroBalanceTokenHidden)
        this += appInfoBlock()
    }

    private fun profileBlock(
        username: Username?,
        isUsernameItemVisible: Boolean
    ): List<SettingsItem> = buildList {
        add(SettingsSpaceSeparatorItem)
        add(SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_profile))
        if (isUsernameItemVisible) {
            addUsernameItem(username)
        }
        add(SignOutButtonItem)
        add(SettingsSpaceSeparatorItem)
    }

    private fun MutableList<SettingsItem>.addUsernameItem(username: Username?) {
        add(
            ComplexSettingsItem(
                nameRes = R.string.settings_item_title_username,
                iconRes = R.drawable.ic_settings_user,
                additionalText = username?.fullUsername
                    ?: resourcesProvider.getString(R.string.settings_item_username_not_reserved),
                hasSeparator = false
            )
        )
    }

    private fun securityBlock(
        isRecoveryKitAvailable: Boolean,
        isBiometricLoginEnabled: Boolean,
        isBiometricLoginAvailable: Boolean,
    ): List<SettingsItem> = listOfNotNull(
        SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_security),
        ComplexSettingsItem(
            nameRes = R.string.settings_item_title_recovery_kit,
            iconRes = R.drawable.ic_settings_shield,
            hasSeparator = true
        ).takeIf { isRecoveryKitAvailable },
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

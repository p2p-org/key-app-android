package org.p2p.wallet.settings.model

import android.content.res.Resources
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.settings.model.SettingsItem.ComplexSettingsItem
import org.p2p.wallet.settings.model.SettingsItem.SettingsGroupTitleItem
import org.p2p.wallet.settings.model.SettingsItem.SettingsSpaceSeparatorItem
import org.p2p.wallet.settings.model.SettingsItem.SignOutButtonItem
import org.p2p.wallet.settings.model.SettingsItem.SwitchSettingsItem
import org.p2p.wallet.settings.model.SettingsItem.TextSettingsItem

class SettingsItemMapper(
    private val resources: Resources
) {
    fun createItems(
        username: Username?,
        isUsernameItemVisible: Boolean,
        countryName: String?,
        isBiometricLoginEnabled: Boolean,
        isZeroBalanceTokenHidden: Boolean,
        isBiometricLoginAvailable: Boolean,
        hasDifferentDeviceShare: Boolean
    ): List<SettingsItem> = buildList {
        this += profileBlock(
            username = username,
            isUsernameItemVisible = isUsernameItemVisible,
            countryName = countryName
        )
        this += securityBlock(
            isBiometricLoginEnabled = isBiometricLoginEnabled,
            isBiometricLoginAvailable = isBiometricLoginAvailable,
            hasDifferentDeviceShare = hasDifferentDeviceShare
        )
        this += appearanceBlock(isZeroBalanceTokenHidden)
        this += communityBlock()
        this += appInfoBlock()
    }

    private fun profileBlock(
        username: Username?,
        countryName: String?,
        isUsernameItemVisible: Boolean
    ): List<SettingsItem> = buildList {
        add(SettingsSpaceSeparatorItem)
        add(SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_profile))
        if (isUsernameItemVisible) {
            addUsernameItem(username)
        }
        addCountrySelectorItem(countryName)
        add(
            ComplexSettingsItem(
                nameRes = R.string.settings_item_title_support,
                iconRes = R.drawable.ic_settings_support,
                hasSeparator = false
            )
        )
        add(SignOutButtonItem)
        add(SettingsSpaceSeparatorItem)
    }

    private fun MutableList<SettingsItem>.addUsernameItem(username: Username?) {
        add(
            ComplexSettingsItem(
                nameRes = R.string.settings_item_title_username,
                iconRes = R.drawable.ic_settings_user,
                additionalText = username?.fullUsername
                    ?: resources.getString(R.string.settings_item_username_not_reserved),
                hasSeparator = true
            )
        )
    }

    private fun MutableList<SettingsItem>.addCountrySelectorItem(countryName: String?) {
        add(
            ComplexSettingsItem(
                nameRes = R.string.settings_item_title_country,
                iconRes = R.drawable.ic_settings_country,
                additionalText = countryName ?: resources.getString(R.string.settings_item_country_not_selected),
                hasSeparator = true
            )
        )
    }

    private fun securityBlock(
        isBiometricLoginEnabled: Boolean,
        isBiometricLoginAvailable: Boolean,
        hasDifferentDeviceShare: Boolean
    ): List<SettingsItem> = listOfNotNull(
        SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_security),
        ComplexSettingsItem(
            nameRes = R.string.settings_item_title_security,
            iconRes = R.drawable.ic_settings_shield,
            hasSeparator = true,
            isBadgeVisible = hasDifferentDeviceShare
        ),
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

    private fun communityBlock(): List<SettingsItem> = listOfNotNull(
        SettingsGroupTitleItem(groupTitleRes = R.string.settings_item_group_title_community),
        ComplexSettingsItem(
            nameRes = R.string.settings_item_title_twitter,
            iconRes = R.drawable.ic_settings_twitter,
            hasSeparator = true
        ),
        ComplexSettingsItem(
            nameRes = R.string.settings_item_title_discord,
            iconRes = R.drawable.ic_settings_discord,
            hasSeparator = false
        ),
        SettingsSpaceSeparatorItem,
    )

    private fun appInfoBlock(): List<SettingsItem> {
        return listOf(
            TextSettingsItem(
                settingNameRes = R.string.settings_app_version,
                iconRes = R.drawable.ic_settings_phone,
                textValue = BuildConfig.VERSION_NAME,
                hasSeparator = false
            ),
            SettingsSpaceSeparatorItem,
        )
    }
}

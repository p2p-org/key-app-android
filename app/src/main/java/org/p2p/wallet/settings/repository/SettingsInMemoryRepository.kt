package org.p2p.wallet.settings.repository

import org.p2p.wallet.R
import org.p2p.wallet.settings.model.SettingsRow

class SettingsInMemoryRepository : SettingsLocalRepository {

    override fun getProfileSettings(username: String): List<SettingsRow> {
        return listOf(
            SettingsRow.Title(R.string.settings_profile),
            SettingsRow.Section(
                titleResId = R.string.settings_username,
                subtitle = username,
                iconRes = R.drawable.ic_settings_user
            ),
//            SettingsRow.Section(
//                titleResId = R.string.settings_address_book,
//                subtitleRes = R.string.settings_address_book_subtitle,
//                iconRes = R.drawable.ic_settings_contacts
//            ),
//            SettingsRow.Section(
//                titleResId = R.string.settings_history,
//                subtitleRes = R.string.settings_history_subtitle,
//                iconRes = R.drawable.ic_settings_history
//            ),
            SettingsRow.Logout()
        )
    }

    override fun getNetworkSettings(networkName: String, feePayerToken: String): List<SettingsRow> {
        return listOf(
            SettingsRow.Title(R.string.settings_security_and_network, isDivider = true),
//            SettingsRow.Section(
//                titleResId = R.string.settings_backup,
//                subtitleRes = R.string.settings_backup_subtitle,
//                iconRes = R.drawable.ic_settings_cloud
//            ),
            SettingsRow.Section(
                titleResId = R.string.settings_wallet_pin,
                subtitleRes = R.string.settings_wallet_pin_subtitle,
                iconRes = R.drawable.ic_settings_pin
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_app_security,
                subtitleRes = R.string.settings_app_security_subtitle,
                iconRes = R.drawable.ic_settings_security
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_network,
                subtitle = networkName,
                iconRes = R.drawable.ic_settings_network
            )
//            SettingsRow.Section(
//                titleResId = R.string.settings_pay_fees_with,
//                subtitle = feePayerToken,
//                iconRes = R.drawable.ic_settings_fees
//            )
        )
    }

    override fun getAppearanceSettings(appVersion: String): List<SettingsRow> {
        return listOf(
            SettingsRow.Title(R.string.settings_appearance, isDivider = true),
//            SettingsRow.Section(
//                titleResId = R.string.settings_staying_up_in_date,
//                subtitleRes = R.string.settings_staying_up_in_date_subtitle,
//                iconRes = R.drawable.ic_settings_notification
//            ),
//            SettingsRow.Section(
//                titleResId = R.string.settings_default_currency,
//                subtitleRes = R.string.settings_default_currency_subtitle,
//                iconRes = R.drawable.ic_settings_currency
//            ),
//            SettingsRow.Section(
//                titleResId = R.string.settings_appearance,
//                subtitleRes = R.string.settings_appearance_subtitle,
//                iconRes = R.drawable.ic_settings_appearance
//            ),
            SettingsRow.Section(
                titleResId = R.string.settings_zero_balances,
                subtitleRes = R.string.settings_zero_balances_subtitle,
                iconRes = R.drawable.ic_settings_eye,
                isDivider = true
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_app_version,
                subtitle = appVersion,
                iconRes = R.drawable.ic_settings_app_version
            )
        )
    }
}
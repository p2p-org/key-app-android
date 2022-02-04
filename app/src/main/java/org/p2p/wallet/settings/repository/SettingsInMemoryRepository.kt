package org.p2p.wallet.settings.repository

import org.p2p.wallet.R
import org.p2p.wallet.settings.model.SettingItem

class SettingsInMemoryRepository : SettingsLocalRepository {

    override fun getProfileSettings(username: String): List<SettingItem> {
        return listOf(
            SettingItem(
                titleRes = R.string.settings_username,
                subtitleRes = R.string.settings_username,
                iconRes = R.drawable.ic_settings_user
            ),
            SettingItem(
                titleRes = R.string.settings_address_book,
                subtitleRes = R.string.settings_address_book_subtitle,
                iconRes = R.drawable.ic_settings_user
            ),
            SettingItem(
                titleRes = R.string.settings_history,
                subtitleRes = R.string.settings_history_subtitle,
                iconRes = R.drawable.ic_settings_user
            )
        )
    }
}
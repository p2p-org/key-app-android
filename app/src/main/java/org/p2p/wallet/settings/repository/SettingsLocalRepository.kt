package org.p2p.wallet.settings.repository

import org.p2p.wallet.settings.model.SettingItem

interface SettingsLocalRepository {
    fun getProfileSettings(username: String): List<SettingItem>
}
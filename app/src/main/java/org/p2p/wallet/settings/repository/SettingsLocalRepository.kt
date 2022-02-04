package org.p2p.wallet.settings.repository

import org.p2p.wallet.settings.model.SettingsRow

interface SettingsLocalRepository {
    fun getProfileSettings(username: String): List<SettingsRow>
    fun getNetworkSettings(networkName: String, feePayerToken: String): List<SettingsRow>
    fun getAppearanceSettings(appVersion: String): List<SettingsRow>
}
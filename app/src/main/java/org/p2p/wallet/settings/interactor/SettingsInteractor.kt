package org.p2p.wallet.settings.interactor

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.settings.repository.SettingsLocalRepository

private const val KEY_HIDDEN_ZERO_BALANCE = "KEY_HIDDEN_ZERO_BALANCE"

class SettingsInteractor(
    private val localRepository: SettingsLocalRepository,
    private val sharedPreferences: SharedPreferences,
    private val environmentManager: EnvironmentManager
) {
    fun getProfileSettings(username: String) = localRepository.getProfileSettings(username)

    fun getNetworkSettings(): List<SettingsRow> {
        val networkName = environmentManager.loadEnvironment().name
        val tokenName = Token.SOL_SYMBOL
        return localRepository.getNetworkSettings(networkName, tokenName)
    }

    fun getEnvironment() = environmentManager.loadEnvironment()

    fun getAppearanceSettings(): List<SettingsRow> {
        // TODO provide app version
        return localRepository.getAppearanceSettings("1")
    }

    fun setZeroBalanceHidden(isHidden: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_HIDDEN_ZERO_BALANCE, isHidden) }
    }

    fun isZerosHidden(): Boolean = sharedPreferences.getBoolean(KEY_HIDDEN_ZERO_BALANCE, true)
}
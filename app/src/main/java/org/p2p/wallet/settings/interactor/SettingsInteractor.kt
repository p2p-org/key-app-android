package org.p2p.wallet.settings.interactor

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.wallet.settings.repository.SettingsLocalRepository

private const val KEY_HIDDEN_ZERO_BALANCE = "KEY_HIDDEN_ZERO_BALANCE"

class SettingsInteractor(
    private val localRepository: SettingsLocalRepository,
    private val sharedPreferences: SharedPreferences
) {
    fun getProfileSettings(username: String) = localRepository.getProfileSettings(username)

    fun setZeroBalanceHidden(isHidden: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_HIDDEN_ZERO_BALANCE, isHidden) }
    }

    fun isZerosHidden(): Boolean = sharedPreferences.getBoolean(KEY_HIDDEN_ZERO_BALANCE, true)
}
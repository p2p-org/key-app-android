package org.p2p.wallet.settings.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.wallet.auth.interactor.AuthInteractor

private const val KEY_HIDDEN_ZERO_BALANCE = "KEY_HIDDEN_ZERO_BALANCE"
private const val KEY_CONFIRMATION_REQUIRED = "KEY_CONFIRMATION_REQUIRED"

class SettingsInteractor(
    private val sharedPreferences: SharedPreferences,
    private val authInteractor: AuthInteractor
) {

    fun setZeroBalanceHidden(isHidden: Boolean) {
        sharedPreferences.edit(commit = true) { putBoolean(KEY_HIDDEN_ZERO_BALANCE, isHidden) }
    }

    fun areZerosHidden(): Boolean = sharedPreferences.getBoolean(KEY_HIDDEN_ZERO_BALANCE, true)

    fun isBiometricsConfirmationEnabled(): Boolean {
        val isConfirmationRequired = sharedPreferences.getBoolean(KEY_CONFIRMATION_REQUIRED, true)
        return authInteractor.isFingerprintEnabled() && isConfirmationRequired
    }

    fun setBiometricsConfirmationEnabled(isEnable: Boolean) {
        sharedPreferences.edit(commit = true) {
            putBoolean(KEY_CONFIRMATION_REQUIRED, isEnable)
        }
    }
}

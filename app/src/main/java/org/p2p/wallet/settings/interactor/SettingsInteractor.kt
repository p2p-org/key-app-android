package org.p2p.wallet.settings.interactor

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.wallet.auth.interactor.AuthInteractor

private const val KEY_HIDDEN_ZERO_BALANCE = "KEY_HIDDEN_ZERO_BALANCE"
private const val KEY_CONFIRMATION_REQUIRED = "KEY_CONFIRMATION_REQUIRED"

class SettingsInteractor(
    private val sharedPreferences: SharedPreferences,
    private val authInteractor: AuthInteractor
) {

    fun setZeroBalanceHidden(isHidden: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_HIDDEN_ZERO_BALANCE, isHidden) }
    }

    fun isZerosHidden(): Boolean = sharedPreferences.getBoolean(KEY_HIDDEN_ZERO_BALANCE, true)

    fun isBiometricsConfirmationEnabled(): Boolean {
        val isConfirmationRequired = sharedPreferences.getBoolean(KEY_CONFIRMATION_REQUIRED, true)
        return authInteractor.isFingerprintEnabled() && isConfirmationRequired
    }

    fun setBiometricsConfirmationEnabled(isEnable: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_CONFIRMATION_REQUIRED, isEnable)
        }
    }
}

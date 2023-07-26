package org.p2p.wallet.settings.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.tokenservice.TokenServiceCoordinator

class SettingsInteractor(
    private val sharedPreferences: SharedPreferences,
    private val authInteractor: AuthInteractor,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
) {

    companion object {
        const val KEY_HIDDEN_ZERO_BALANCE = "KEY_HIDDEN_ZERO_BALANCE"
    }

    fun setZeroBalanceHidden(isHidden: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_HIDDEN_ZERO_BALANCE, isHidden) }
        tokenServiceCoordinator.refresh()
    }

    fun areZerosHidden(): Boolean = sharedPreferences.getBoolean(KEY_HIDDEN_ZERO_BALANCE, true)

    fun isBiometricLoginEnabled(): Boolean = authInteractor.getBiometricStatus() == BiometricStatus.ENABLED

    fun isBiometricLoginAvailable(): Boolean {
        val notAvailableStates = setOf(
            BiometricStatus.NO_HARDWARE,
            BiometricStatus.NO_REGISTERED_BIOMETRIC
        )
        return authInteractor.getBiometricStatus() !in notAvailableStates
    }
}

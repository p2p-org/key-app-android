package org.p2p.wallet.settings.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import timber.log.Timber
import org.p2p.core.utils.fromJsonReified
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.auth.model.CountryCode

class SettingsInteractor(
    private val sharedPreferences: SharedPreferences,
    private val authInteractor: AuthInteractor,
    private val gson: Gson,
) {
    companion object {
        const val KEY_HIDDEN_ZERO_BALANCE = "KEY_HIDDEN_ZERO_BALANCE"
        const val KEY_USER_COUNTRY_CODE = "KEY_USER_COUNTRY_CODE"
    }

    var userCountryCode: CountryCode? = null
        get() {
            return field ?: try {
                sharedPreferences
                    .getString(KEY_USER_COUNTRY_CODE, null)
                    ?.let(gson::fromJsonReified)
            } catch (e: Throwable) {
                // this try-catch will help if CountryCode structure is changed
                Timber.i(e, "Unable to decode user country code from settings")
                null
            }
        }
        set(value) {
            field = value
            sharedPreferences.edit { putString(KEY_USER_COUNTRY_CODE, gson.toJson(value)) }
        }

    fun setZeroBalanceHidden(isHidden: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_HIDDEN_ZERO_BALANCE, isHidden) }
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

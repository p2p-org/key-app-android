package org.p2p.wallet.settings.interactor

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import timber.log.Timber
import org.p2p.core.utils.fromJsonReified
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.settings.mapper.SettingsEntityMapper
import org.p2p.wallet.settings.model.UserCountrySettingsEntity

class SettingsInteractor(
    private val sharedPreferences: SharedPreferences,
    private val authInteractor: AuthInteractor,
    private val gson: Gson,
    private val settingsEntityMapper: SettingsEntityMapper,
) {
    companion object {
        const val KEY_HIDDEN_ZERO_BALANCE = "KEY_HIDDEN_ZERO_BALANCE"
        const val KEY_USER_COUNTRY_CODE = "KEY_USER_COUNTRY_CODE"
    }

    var userCountryCode: CountryCode? = null
        get() {
            return field ?: getUserCountryCodeOrNull()
        }
        set(value) {
            field = value
            saveUserCountryCode(value)
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

    private fun getUserCountryCodeOrNull(): CountryCode? {
        val rawData = sharedPreferences.getString(KEY_USER_COUNTRY_CODE, null)
        val entity: UserCountrySettingsEntity? = try {
            rawData?.let(gson::fromJsonReified)
        } catch (e: Throwable) {
            // this try-catch will help if CountryCode structure is changed
            Timber.e(e, "Unable to decode user country from settings:\n${rawData ?: "null"}")
            null
        }

        return entity?.let(settingsEntityMapper::fromEntity)
    }

    private fun saveUserCountryCode(value: CountryCode?) {
        val entity = value?.let(settingsEntityMapper::toEntity)
        sharedPreferences.edit { putString(KEY_USER_COUNTRY_CODE, gson.toJson(entity)) }
    }
}

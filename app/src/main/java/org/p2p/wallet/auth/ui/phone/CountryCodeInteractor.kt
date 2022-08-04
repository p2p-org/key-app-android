package org.p2p.wallet.auth.ui.phone

import android.content.Context
import android.content.res.Resources
import android.telephony.TelephonyManager
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeLocalRepository

class CountryCodeInteractor(
    private val countryCodeLocalRepository: CountryCodeLocalRepository,
    private val phoneNumberUtil: PhoneNumberUtil
) {

    suspend fun detectCountryCodeByLocale(resources: Resources): CountryCode? {
        return try {
            val localeCountryIso = resources.configuration.locale.country
            getCountryForISO(localeCountryIso)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun detectCountryCodeBySimCard(context: Context): CountryCode? {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simCountryISO = telephonyManager.simCountryIso
            getCountryForISO(simCountryISO)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun detectCountryCodeByNetwork(context: Context): CountryCode? {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val networkCountryIso = telephonyManager.networkCountryIso
            getCountryForISO(networkCountryIso)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getCountryForISO(nameCode: String): CountryCode? {
        val allCountries = countryCodeLocalRepository.getCountryCodes()
        return allCountries.firstOrNull { it.nameCode.equals(nameCode, ignoreCase = true) }
    }

    suspend fun findCountryForPhoneCode(phoneCode: String): CountryCode? {
        return try {
            val allCountries = countryCodeLocalRepository.getCountryCodes()
            val countryCode = allCountries.firstOrNull { it.phoneCode == phoneCode }
            countryCode
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCountries(): List<CountryCode> = countryCodeLocalRepository.getCountryCodes()

    fun isValidNumberForRegion(countryCode: String, phoneNumber: String): Boolean {
        return try {
            val validatePhoneNumber = phoneNumberUtil.parse(phoneNumber, countryCode.uppercase())
            val isValidNumber = phoneNumberUtil.isValidNumber(validatePhoneNumber)
            isValidNumber
        } catch (countryNotFound: Exception) {
            return false
        }
    }
}

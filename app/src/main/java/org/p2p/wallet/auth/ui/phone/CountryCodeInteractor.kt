package org.p2p.wallet.auth.ui.phone

import android.content.Context
import android.content.res.Resources
import android.telephony.TelephonyManager
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.auth.ui.phone.repository.CountryCodeLocalRepository

class CountryCodeInteractor(
    private val countryCodeLocalRepository: CountryCodeLocalRepository,
    private val phoneNumberUtil: PhoneNumberUtil
) {

    fun detectLocaleCountry(resources: Resources): CountryCode? {
        return try {
            val localeCountryIso = resources.configuration.locale.country
            getCountryForISO(localeCountryIso)
        } catch (e: Exception) {
            null
        }
    }

    fun detectSimCountry(context: Context): CountryCode? {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simCountryISO = telephonyManager.simCountryIso
            getCountryForISO(simCountryISO)
        } catch (e: Exception) {
            null
        }
    }

    fun detectNetworkCountry(context: Context): CountryCode? {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val networkCountryIso = telephonyManager.networkCountryIso
            getCountryForISO(networkCountryIso)
        } catch (e: Exception) {
            null
        }
    }

    private fun getCountryForISO(nameCode: String): CountryCode? {
        val allCountries = countryCodeLocalRepository.getCountryCodes()
        return allCountries.firstOrNull { it.nameCode.equals(nameCode, ignoreCase = true) }
    }

    fun findCountryForPhoneCode(phoneCode: String): CountryCode? {
        val allCountries = countryCodeLocalRepository.getCountryCodes()
        return allCountries.firstOrNull { it.phoneCode == phoneCode }
    }

    fun getCountries() = countryCodeLocalRepository.getCountryCodes()

    fun findCountryForPhoneNumber(phoneNumber: String): CountryCode? {
        return try {
            val validateNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+$phoneNumber"
            val phoneNumber = phoneNumberUtil.parse(validateNumber, null)
            findCountryForPhoneCode(phoneNumber.countryCode.toString())
        } catch (e: Exception) {
            null
        }
    }

    fun isValidNumberForRegion(regionCode: String, phoneNumber: String): Boolean {
        return try {
            val phoneNumber = phoneNumberUtil.parse(phoneNumber, null)
            phoneNumberUtil.isValidNumberForRegion(phoneNumber, regionCode)
        } catch (e: Exception) {
            return false
        }
    }
}

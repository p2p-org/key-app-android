package org.p2p.wallet.auth.ui.phone

import android.content.Context
import android.content.res.Resources
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.util.SparseArray
import androidx.core.util.isEmpty
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.auth.ui.phone.model.SameCountriesCodeGroup
import org.p2p.wallet.auth.ui.phone.repository.CountryCodeLocalRepository

class CountryCodeInteractor(
    private val phoneUtils: PhoneNumberUtil,
    private val phoneNumberType: PhoneNumberUtil.PhoneNumberType,
    private val countryCodeLocalRepository: CountryCodeLocalRepository
) {

    private var sameCountryCodeGroup = SameCountriesCodeGroup(SparseArray())

    fun getHint(
        selectedCountryNameCode: String,
        selectedCountryCodeWithPlus: String
    ): String {
        var formattedNumber = ""
        var exampleNumber = phoneUtils.getExampleNumberForType(selectedCountryNameCode, phoneNumberType)
        if (exampleNumber != null) {
            formattedNumber = PhoneNumberUtils.formatNumber(
                selectedCountryCodeWithPlus + formattedNumber,
                selectedCountryNameCode
            )
            if (formattedNumber != null) {
                formattedNumber = formattedNumber.substring(selectedCountryCodeWithPlus.length).trim()
            }
        }
        return formattedNumber
    }

    fun getCountryCodeGroups(): SameCountriesCodeGroup {
        if (sameCountryCodeGroup.groups.isEmpty()) {
            sameCountryCodeGroup = CountryPickerParsingManager.getSameCountriesCodeGroup()
        }
        return sameCountryCodeGroup
    }

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
}

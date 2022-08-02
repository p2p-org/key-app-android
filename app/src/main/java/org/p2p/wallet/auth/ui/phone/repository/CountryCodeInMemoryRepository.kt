package org.p2p.wallet.auth.ui.phone.repository

import android.content.res.Resources
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.auth.ui.phone.CountryPickerParsingManager
import org.p2p.wallet.auth.ui.phone.model.CountryCode

class CountryCodeInMemoryRepository(
    private val resources: Resources
) : CountryCodeLocalRepository {

    private val countryCodesFlow = MutableStateFlow<List<CountryCode>>(emptyList())
    private val countryCodeMask = MutableStateFlow<Map<String, String>>(hashMapOf())

    override fun getCountryCodes(): List<CountryCode> {
        if (countryCodesFlow.value.isEmpty()) {
            val countries = CountryPickerParsingManager.readCountriesFromXml(resources)
            countries.forEach { country ->
                country.mask = getMaskForCountryCode(country.nameCode.uppercase())
            }
            countryCodesFlow.value = countries
        }
        return countryCodesFlow.value
    }

    private fun getMaskForCountryCode(countryCode: String): String {
        if (countryCodeMask.value.isEmpty()) {
            countryCodeMask.value = CountryPickerParsingManager.readCountriesMasks(resources)
        }
        return countryCodeMask.value[countryCode].orEmpty()
    }
}

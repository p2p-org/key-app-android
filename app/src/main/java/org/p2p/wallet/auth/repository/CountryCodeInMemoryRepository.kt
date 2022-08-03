package org.p2p.wallet.auth.repository

import org.p2p.wallet.auth.ui.phone.CountryPickerParser
import org.p2p.wallet.auth.model.CountryCode

class CountryCodeInMemoryRepository(
    private val parser: CountryPickerParser
) : CountryCodeLocalRepository {

    private var countryCodes = mutableListOf<CountryCode>()
    private var countryCodeMask = mutableMapOf<String, String>()

    override suspend fun getCountryCodes(): List<CountryCode> {
        if (countryCodes.isEmpty()) {
            val countries = parser.readCountriesFromXml()
            countries.forEach { country ->
                country.mask = getMaskForCountryCode(country.nameCode.uppercase())
            }
            countryCodes = countries.toMutableList()
        }
        return countryCodes
    }

    private suspend fun getMaskForCountryCode(countryCode: String): String {
        if (countryCodeMask.isEmpty()) {
            countryCodeMask = parser.readCountriesMasks().toMutableMap()
        }
        return countryCodeMask[countryCode].orEmpty()
    }
}

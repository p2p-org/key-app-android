package org.p2p.wallet.auth.repository

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.p2p.wallet.auth.ui.phone.CountryPickerParser
import org.p2p.wallet.auth.model.CountryCode

class CountryCodeInMemoryRepository(
    private val parser: CountryPickerParser,
    private val phoneNumberUtil: PhoneNumberUtil
) : CountryCodeLocalRepository {

    private var countryCodes = mutableListOf<CountryCode>()
    private var countryCodeMask = mutableMapOf<String, String>()

    override suspend fun getCountryCodes(): List<CountryCode> {
        if (countryCodes.isEmpty()) {
            val countries = parser.readCountriesFromXml()
            countries.forEach { country ->
                country.mask = getMaskForCountryCode(country.nameCode, country.phoneCode)
            }
            countryCodes = countries.toMutableList()
        }
        return countryCodes
    }

    private fun getMaskForCountryCode(countryCode: String, phoneCode: String): String {
        return try {
            val exampleNumber =
                phoneNumberUtil.getExampleNumber(countryCode)
            val internationalFormat =
                phoneNumberUtil.format(exampleNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            internationalFormat.replace("+$phoneCode", "")
        } catch (e: Exception) {
            countryCodeMask[countryCode].orEmpty()
        }
    }
}

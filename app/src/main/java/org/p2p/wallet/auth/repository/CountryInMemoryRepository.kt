package org.p2p.wallet.auth.repository

import org.p2p.wallet.auth.model.CountryCode

class CountryInMemoryRepository(
    private val countryCodeRepository: CountryCodeLocalRepository,
) : CountryRepository {
    override suspend fun getAllCountries(): List<Country> {
        return countryCodeRepository.getCountryCodes()
            .map { it.extractCountry() }
    }

    override suspend fun detectCountryOrDefault(): Country {
        val detectedCountryCode = countryCodeRepository.detectCountryCodeBySimCard()
            ?: countryCodeRepository.detectCountryCodeByNetwork()
            ?: countryCodeRepository.detectCountryCodeByLocale()

        return detectedCountryCode?.extractCountry() ?: defaultCountry()
    }

    private fun CountryCode.extractCountry(): Country = Country(countryName, flagEmoji, nameCode)

    private suspend fun defaultCountry(): Country = getAllCountries().first()
}

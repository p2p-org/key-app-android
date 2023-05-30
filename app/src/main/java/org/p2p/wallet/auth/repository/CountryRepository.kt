package org.p2p.wallet.auth.repository

interface CountryRepository {
    suspend fun getAllCountries(): List<Country>
    suspend fun detectCountryOrDefault(): Country

    /**
     * Returns phone mask for given country code or null
     * @see res/raw/phone_masks.txt
     */
    suspend fun findPhoneMaskByCountry(country: Country): String?
}

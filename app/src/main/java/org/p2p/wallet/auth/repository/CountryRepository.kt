package org.p2p.wallet.auth.repository

interface CountryRepository {
    suspend fun getAllCountries(): List<Country>
    suspend fun detectCountryOrDefault(): Country

    /**
     * Find [Country] by ISO 3166-1 alpha-3 code (US, DE, RU, etc.)
     */
    suspend fun findCountryByNameCode(countyCode: String): Country?

    /**
     * Returns phone mask for given country code or null
     * @see res/raw/phone_masks.txt
     */
    suspend fun findPhoneMaskByCountry(country: Country): String?
}

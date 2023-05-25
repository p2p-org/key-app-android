package org.p2p.wallet.auth.repository

interface CountryRepository {
    suspend fun getAllCountries(): List<Country>
    suspend fun detectCountryOrDefault(): Country
}

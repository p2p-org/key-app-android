package org.p2p.wallet.auth.repository

import org.p2p.wallet.auth.model.CountryCode

interface CountryCodeLocalRepository {
    suspend fun getCountryCodes(): List<CountryCode>
    suspend fun detectCountryCodeByLocale(): CountryCode?
    suspend fun detectCountryCodeByNetwork(): CountryCode?
    suspend fun detectCountryCodeBySimCard(): CountryCode?
    fun findCountryCodeByPhoneCode(phoneCode: String): CountryCode?
    fun findCountryCodeByIsoAlpha2(nameCode: String): CountryCode?
    fun findCountryCodeByIsoAlpha3(nameCode: String): CountryCode?
    fun isValidNumberForRegion(phoneNumber: String, countryCode: String): Boolean
}

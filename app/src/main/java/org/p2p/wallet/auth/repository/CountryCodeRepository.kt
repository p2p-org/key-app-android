package org.p2p.wallet.auth.repository

import org.p2p.wallet.auth.model.CountryCode

interface CountryCodeRepository {
    suspend fun getCountryCodes(): List<CountryCode>
    suspend fun detectCountryCodeByLocale(): CountryCode?
    suspend fun detectCountryCodeByNetwork(): CountryCode?
    suspend fun detectCountryCodeBySimCard(): CountryCode?
    suspend fun detectCountryOrDefault(): CountryCode
    fun findCountryCodeByPhoneCode(phoneCode: String): CountryCode?
    fun findCountryCodeByIsoAlpha2(nameCode: String): CountryCode?
    fun findCountryCodeByIsoAlpha3(nameCode: String): CountryCode?
    fun isValidNumberForRegion(phoneNumber: String, regionCode: String): Boolean
}

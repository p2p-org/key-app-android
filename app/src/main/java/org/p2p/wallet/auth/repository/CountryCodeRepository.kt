package org.p2p.wallet.auth.repository

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.PhoneNumberWithCode

interface CountryCodeRepository {
    suspend fun getCountryCodes(): List<CountryCode>
    suspend fun detectCountryCodeByLocale(): CountryCode?
    suspend fun detectCountryCodeByNetwork(): CountryCode?
    suspend fun detectCountryCodeBySimCard(): CountryCode?
    suspend fun detectCountryOrDefault(): CountryCode

    /**
     * @param phoneCode - phone country code with or without plus sign
     */
    fun findCountryCodeByPhoneCode(phoneCode: String): CountryCode?
    fun findCountryCodeByIsoAlpha2(nameCode: String): CountryCode?
    fun findCountryCodeByIsoAlpha3(nameCode: String): CountryCode?
    fun isValidNumberForRegion(phoneNumber: String, regionCode: String): Boolean
    fun parsePhoneNumber(number: String, defaultRegionAlpha2: String = "US"): PhoneNumberWithCode?
}

package org.p2p.wallet.countrycode.repository

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.countrycode.ExternalCountryCodeError
import org.p2p.wallet.countrycode.model.ExternalCountryCode
import org.p2p.wallet.countrycode.model.PhoneNumberWithCountryCode

interface ExternalCountryCodeRepository {
    suspend fun getCountryCodes(): Set<ExternalCountryCode>
    suspend fun detectCountryCodeByLocale(): ExternalCountryCode?
    suspend fun detectCountryCodeByNetwork(): ExternalCountryCode?
    suspend fun detectCountryCodeBySimCard(): ExternalCountryCode?
    suspend fun detectCountryOrDefault(): ExternalCountryCode

    /**
     * @param phoneCode - phone country code with or without plus sign
     */
    suspend fun findCountryCodeByPhoneCode(phoneCode: String): ExternalCountryCode?
    suspend fun findCountryCodeByIsoAlpha2(codeAlpha2: String): ExternalCountryCode?
    suspend fun findCountryCodeByIsoAlpha3(codeAlpha3: String): ExternalCountryCode?
    suspend fun isValidNumberForRegion(phoneNumber: String, regionCodeAlpha2: String): Boolean
    suspend fun parsePhoneNumber(phoneNumber: String, defaultRegionAlpha2: String = "US"): PhoneNumberWithCountryCode?

    @Throws(ExternalCountryCodeError::class, IllegalStateException::class)
    suspend fun isMoonpaySupported(countryCode: CountryCode): Boolean

    @Throws(ExternalCountryCodeError::class, IllegalStateException::class)
    suspend fun isStrigaSupported(countryCode: CountryCode): Boolean
}

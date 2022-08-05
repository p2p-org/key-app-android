package org.p2p.wallet.auth.ui.phone

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeLocalRepository

class CountryCodeInteractor(private val countryCodeLocalRepository: CountryCodeLocalRepository) {

    suspend fun detectCountryCodeByLocale(): CountryCode? =
        countryCodeLocalRepository.detectCountryCodeByLocale()

    suspend fun detectCountryCodeBySimCard(): CountryCode? =
        countryCodeLocalRepository.detectCountryCodeBySimCard()

    suspend fun detectCountryCodeByNetwork(): CountryCode? =
        countryCodeLocalRepository.detectCountryCodeByNetwork()

    fun findCountryForPhoneCode(phoneCode: String): CountryCode? =
        countryCodeLocalRepository.findCountryCodeByPhoneCode(phoneCode)

    suspend fun getCountries(): List<CountryCode> = countryCodeLocalRepository.getCountryCodes()

    fun isValidNumberForRegion(countryCode: String, phoneNumber: String): Boolean =
        countryCodeLocalRepository.isValidNumberForRegion(phoneNumber, countryCode)
}

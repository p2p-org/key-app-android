package org.p2p.wallet.auth.repository

import android.content.Context
import android.telephony.TelephonyManager
import io.michaelrocks.libphonenumber.android.Phonenumber.PhoneNumber
import timber.log.Timber
import kotlinx.coroutines.withContext
import org.p2p.wallet.auth.gateway.parser.CountryCodeXmlParser
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.PhoneNumberWithCode
import org.p2p.core.dispatchers.CoroutineDispatchers

class CountryCodeInMemoryRepository(
    private val dispatchers: CoroutineDispatchers,
    private val context: Context,
    private val countryCodeHelper: CountryCodeXmlParser
) : CountryCodeRepository {

    private val allCountryCodes: List<CountryCode> by lazy {
        countryCodeHelper.parserCountryCodesFromXmlFile()
    }

    override suspend fun getCountryCodes(): List<CountryCode> = allCountryCodes

    override suspend fun detectCountryCodeByLocale(): CountryCode? = withContext(dispatchers.io) {
        try {
            val localeCountryIsoAlpha2 = context.resources.configuration.locale.country
            findCountryCodeByIsoAlpha2(localeCountryIsoAlpha2)
        } catch (error: Throwable) {
            Timber.i(error, "Detecting country code by locale failed")
            null
        }
    }

    override suspend fun detectCountryCodeByNetwork(): CountryCode? = withContext(dispatchers.io) {
        try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val networkCountryIso = telephonyManager.networkCountryIso
            findCountryCodeByIsoAlpha2(networkCountryIso)
        } catch (error: Throwable) {
            Timber.i(error, "Detecting country code by network failed")
            null
        }
    }

    override suspend fun detectCountryCodeBySimCard(): CountryCode? = withContext(dispatchers.io) {
        try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val simCountryISOAlpha2 = telephonyManager.simCountryIso
            findCountryCodeByIsoAlpha2(simCountryISOAlpha2)
        } catch (error: Throwable) {
            Timber.i(error, "Detecting country code by sim card failed")
            null
        }
    }

    override fun findCountryCodeByPhoneCode(phoneCode: String): CountryCode? {
        val rawPhoneCode = phoneCode.replace("+", "")
        return allCountryCodes.firstOrNull { it.phoneCode == rawPhoneCode }
    }

    override fun findCountryCodeByIsoAlpha2(nameCode: String): CountryCode? =
        allCountryCodes.firstOrNull { it.nameCodeAlpha2.equals(nameCode, ignoreCase = true) }

    override fun findCountryCodeByIsoAlpha3(nameCode: String): CountryCode? =
        allCountryCodes.firstOrNull { it.nameCodeAlpha3.equals(nameCode, ignoreCase = true) }

    override fun isValidNumberForRegion(phoneNumber: String, regionCode: String): Boolean =
        countryCodeHelper.isValidNumberForRegion(phoneNumber, regionCode)

    override fun parsePhoneNumber(number: String, defaultRegionAlpha2: String): PhoneNumberWithCode? {
        val phoneNumber: PhoneNumber = countryCodeHelper.parsePhoneNumber(number, defaultRegionAlpha2) ?: return null
        val countryCode = findCountryCodeByPhoneCode(phoneNumber.countryCode.toString()) ?: return null

        return PhoneNumberWithCode(countryCode, phoneNumber.nationalNumber.toString())
    }

    override suspend fun detectCountryOrDefault(): CountryCode {
        return detectCountryCodeBySimCard()
            ?: detectCountryCodeByLocale()
            ?: detectCountryCodeByNetwork()
            ?: allCountryCodes.first()
    }
}

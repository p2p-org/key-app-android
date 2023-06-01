package org.p2p.wallet.auth.repository

import android.content.Context
import android.telephony.TelephonyManager
import timber.log.Timber
import kotlinx.coroutines.withContext
import org.p2p.wallet.auth.gateway.parser.CountryCodeXmlParser
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers

class CountryCodeInMemoryRepository(
    private val dispatchers: CoroutineDispatchers,
    private val context: Context,
    private val countryCodeHelper: CountryCodeXmlParser
) : CountryCodeLocalRepository {

    private val allCountryCodes: List<CountryCode> by lazy {
        countryCodeHelper.parserCountryCodesFromXmlFile()
    }

    override suspend fun getCountryCodes(): List<CountryCode> = allCountryCodes

    override suspend fun detectCountryCodeByLocale(): CountryCode? = withContext(dispatchers.io) {
        try {
            val localeCountryIso = context.resources.configuration.locale.country
            getCountryForIso(localeCountryIso)
        } catch (error: Throwable) {
            Timber.i(error, "Detecting country code by locale failed")
            null
        }
    }

    override suspend fun detectCountryCodeByNetwork(): CountryCode? = withContext(dispatchers.io) {
        try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val networkCountryIso = telephonyManager.networkCountryIso
            getCountryForIso(networkCountryIso)
        } catch (error: Throwable) {
            Timber.i(error, "Detecting country code by network failed")
            null
        }
    }

    override suspend fun detectCountryCodeBySimCard(): CountryCode? = withContext(dispatchers.io) {
        try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val simCountryISO = telephonyManager.simCountryIso
            getCountryForIso(simCountryISO)
        } catch (error: Throwable) {
            Timber.i(error, "Detecting country code by sim card failed")
            null
        }
    }

    override fun findCountryCodeByPhoneCode(phoneCode: String): CountryCode? =
        allCountryCodes.firstOrNull { it.phoneCode == phoneCode }

    override fun isValidNumberForRegion(phoneNumber: String, countryCode: String): Boolean =
        countryCodeHelper.isValidNumberForRegion(phoneNumber, countryCode)

    private suspend fun getCountryForIso(nameCode: String): CountryCode? = withContext(dispatchers.io) {
        allCountryCodes.firstOrNull { it.nameCode.equals(nameCode, ignoreCase = true) }
    }
}

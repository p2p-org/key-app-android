package org.p2p.wallet.auth.repository

import android.content.Context
import android.telephony.TelephonyManager
import kotlinx.coroutines.withContext
import org.p2p.wallet.auth.gateway.parser.CountryCodeHelper
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import timber.log.Timber

class CountryCodeInMemoryRepository(
    private val dispatchers: CoroutineDispatchers,
    private val context: Context,
    private val countryCodeHelper: CountryCodeHelper
) : CountryCodeLocalRepository {

    private val allCountryCodes = mutableListOf<CountryCode>()

    override suspend fun getCountryCodes(): List<CountryCode> = allCountryCodes

    override suspend fun detectCountryCodeByLocale(): CountryCode? = withContext(dispatchers.io) {
        try {
            val localeCountryIso = context.resources.configuration.locale.country
            getCountryForIso(localeCountryIso)
        } catch (error: Exception) {
            Timber.i(error, "Detecting country code by locale failed")
            null
        }
    }

    override suspend fun detectCountryCodeByNetwork(): CountryCode? = withContext(dispatchers.io) {
        try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val networkCountryIso = telephonyManager.networkCountryIso
            getCountryForIso(networkCountryIso)
        } catch (error: Exception) {
            Timber.i(error, "Detecting country code by network failed")
            null
        }
    }

    override suspend fun detectCountryCodeBySimCard(): CountryCode? = withContext(dispatchers.io) {
        try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val simCountryISO = telephonyManager.simCountryIso
            getCountryForIso(simCountryISO)
        } catch (error: Exception) {
            Timber.i(error, "Detecting country code by sim card failed")
            null
        }
    }

    override fun findCountryCodeByPhoneCode(phoneCode: String): CountryCode? =
        allCountryCodes.firstOrNull { it.phoneCode == phoneCode }

    override fun isValidNumberForRegion(phoneNumber: String, countryCode: String): Boolean =
        countryCodeHelper.isValidNumberForRegion(phoneNumber, countryCode)

    private suspend fun getCountryForIso(nameCode: String): CountryCode? = withContext(dispatchers.io) {
        if (allCountryCodes.isEmpty()) {
            readCountriesFromXml()
        }
        allCountryCodes.firstOrNull { it.nameCode.equals(nameCode, ignoreCase = true) }
    }

    private suspend fun readCountriesFromXml(): Boolean = withContext(dispatchers.io) {
        allCountryCodes.clear()
        allCountryCodes.addAll(countryCodeHelper.parserCountryCodesFromXmlFile())
    }
}

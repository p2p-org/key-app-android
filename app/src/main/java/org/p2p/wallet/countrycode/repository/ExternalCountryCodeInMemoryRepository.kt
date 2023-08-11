package org.p2p.wallet.countrycode.repository

import android.content.Context
import android.telephony.TelephonyManager
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import timber.log.Timber
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.countrycode.ExternalCountryCodeLoader
import org.p2p.wallet.countrycode.model.ExternalCountryCode
import org.p2p.wallet.countrycode.model.PhoneNumberWithCountryCode

class ExternalCountryCodeInMemoryRepository(
    private val context: Context,
    private val loader: ExternalCountryCodeLoader,
    private val phoneNumberUtil: PhoneNumberUtil
) : ExternalCountryCodeRepository {

    private val allCountryCodes: MutableSet<ExternalCountryCode> = mutableSetOf()
    private val mutex = Mutex()

    private suspend fun ensureCountriesAreLoaded() {
        if (allCountryCodes.isEmpty()) {
            // lock mutex to prevent concurrent modification
            mutex.withLock {
                // double-checking to prevent reentrant loading
                if (allCountryCodes.isEmpty()) {
                    val data = loader.loadAndSaveFile()
                    allCountryCodes.addAll(data)
                }
            }
        }
    }

    override suspend fun getCountryCodes(): Set<ExternalCountryCode> {
        ensureCountriesAreLoaded()
        return allCountryCodes
    }

    override suspend fun detectCountryCodeByLocale(): ExternalCountryCode? {
        return try {
            val localeCountryIsoAlpha2 = context.resources.configuration.locale.country
            findCountryCodeByIsoAlpha2(localeCountryIsoAlpha2)
        } catch (error: Throwable) {
            Timber.i(error, "Detecting country code by locale failed")
            null
        }
    }

    override suspend fun detectCountryCodeByNetwork(): ExternalCountryCode? {
        return try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val networkCountryIso = telephonyManager.networkCountryIso
            findCountryCodeByIsoAlpha2(networkCountryIso)
        } catch (error: Throwable) {
            Timber.i(error, "Detecting country code by network failed")
            null
        }
    }

    override suspend fun detectCountryCodeBySimCard(): ExternalCountryCode? {
        return try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val simCountryISOAlpha2 = telephonyManager.simCountryIso
            findCountryCodeByIsoAlpha2(simCountryISOAlpha2)
        } catch (error: Throwable) {
            Timber.i(error, "Detecting country code by sim card failed")
            null
        }
    }

    override suspend fun detectCountryOrDefault(): ExternalCountryCode {
        return detectCountryCodeBySimCard()
            ?: detectCountryCodeByLocale()
            ?: detectCountryCodeByNetwork()
            ?: allCountryCodes.first()
    }

    override suspend fun findCountryCodeByPhoneCode(phoneCode: String): ExternalCountryCode? {
        ensureCountriesAreLoaded()

        val rawPhoneCode = phoneCode.replace("+", "").toInt()
        return when (val codeAlpha2 = phoneNumberUtil.getRegionCodeForCountryCode(rawPhoneCode)) {
            "ZZ" -> return null
            "001" -> return null
            else -> allCountryCodes.firstOrNull { it.nameCodeAlpha2.equals(codeAlpha2, ignoreCase = true) }
        }
    }

    override suspend fun findCountryCodeByIsoAlpha2(codeAlpha2: String): ExternalCountryCode? {
        return allCountryCodes.firstOrNull { it.nameCodeAlpha2.equals(codeAlpha2, ignoreCase = true) }
    }

    override suspend fun findCountryCodeByIsoAlpha3(codeAlpha3: String): ExternalCountryCode? {
        return allCountryCodes.firstOrNull { it.nameCodeAlpha3.equals(codeAlpha3, ignoreCase = true) }
    }

    override suspend fun isValidNumberForRegion(phoneNumber: String, regionCodeAlpha2: String): Boolean {
        return try {
            val validatePhoneNumber = phoneNumberUtil.parse(phoneNumber, regionCodeAlpha2.uppercase())
            phoneNumberUtil.isValidNumber(validatePhoneNumber)
        } catch (countryNotFound: Throwable) {
            Timber.i(countryNotFound, "Phone number validation failed")
            return false
        }
    }

    override suspend fun parsePhoneNumber(
        phoneNumber: String,
        defaultRegionAlpha2: String
    ): PhoneNumberWithCountryCode? {
        val number = try {
            phoneNumberUtil.parse(phoneNumber, defaultRegionAlpha2.uppercase())
        } catch (countryNotFound: Throwable) {
            Timber.i(countryNotFound, "Phone number validation failed")
            return null
        }
        val countryCode = findCountryCodeByPhoneCode(number.countryCode.toString()) ?: return null

        return PhoneNumberWithCountryCode(
            countryCode = countryCode,
            phoneCode = number.countryCode.toString(),
            phoneNumberNational = number.nationalNumber.toString(),
            mask = getMaskForCountryCode(countryCode.nameCodeAlpha2, number.countryCode.toString())
        )
    }

    override suspend fun isMoonpaySupported(countryCode: CountryCode): Boolean {
        ensureCountriesAreLoaded()
        val code = findCountryCodeByIsoAlpha3(countryCode.nameCodeAlpha3)
            ?: error("Country code [${countryCode.nameCodeAlpha3}] not found")

        return code.isMoonpayAllowed
    }

    override suspend fun isStrigaSupported(countryCode: CountryCode): Boolean {
        ensureCountriesAreLoaded()
        val code = findCountryCodeByIsoAlpha3(countryCode.nameCodeAlpha3)
            ?: error("Country code [${countryCode.nameCodeAlpha3}] not found")

        return code.isStrigaAllowed
    }

    private fun getMaskForCountryCode(countryCode: String, phoneCode: String): String {
        return try {
            val exampleNumber: Phonenumber.PhoneNumber? = phoneNumberUtil.getExampleNumber(countryCode)
            if (exampleNumber != null) {
                val internationalFormat = phoneNumberUtil.format(
                    exampleNumber,
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                )
                internationalFormat.replace("+$phoneCode", org.p2p.wallet.utils.emptyString())
            } else {
                org.p2p.wallet.utils.emptyString()
            }
        } catch (e: Throwable) {
            Timber.i(e, "Get mask for country code failed")
            org.p2p.wallet.utils.emptyString()
        }
    }
}

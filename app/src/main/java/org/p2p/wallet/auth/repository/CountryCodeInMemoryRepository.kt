package org.p2p.wallet.auth.repository

import android.content.Context
import android.telephony.TelephonyManager
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.utils.emptyString
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber

class CountryCodeInMemoryRepository(
    private val phoneNumberUtil: PhoneNumberUtil,
    private val appScope: AppScope,
    private val dispatchers: CoroutineDispatchers,
    private val context: Context
) : CountryCodeLocalRepository {

    private val countryCodes = mutableListOf<CountryCode>()
    private var countryCodeMask = mutableMapOf<String, String>()

    private val KEY_COUNTRY = "country"
    private val KEY_NAME_CODE = "name_code"
    private val KEY_PHONE_CODE = "phone_code"
    private val KEY_NAME = "name"
    private val KEY_FLAG_EMOJI = "flag_emoji"

    override suspend fun getCountryCodes(): List<CountryCode> = countryCodes

    override suspend fun detectCountryCodeByLocale(): CountryCode? {
        return try {
            val localeCountryIso = context.resources.configuration.locale.country
            getCountryForISO(localeCountryIso)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun detectCountryCodeByNetwork(): CountryCode? {
        return try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val networkCountryIso = telephonyManager.networkCountryIso
            getCountryForISO(networkCountryIso)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun detectCountryCodeBySimCard(): CountryCode? {
        return try {
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val simCountryISO = telephonyManager.simCountryIso
            getCountryForISO(simCountryISO)
        } catch (e: Exception) {
            null
        }
    }

    override fun findCountryCodeForPhoneCode(phoneCode: String): CountryCode? {
        return try {
            val allCountries = countryCodes
            val countryCode = allCountries.firstOrNull { it.phoneCode == phoneCode }
            countryCode
        } catch (e: Exception) {
            null
        }
    }

    override fun isValidNumberForRegion(phoneNumber: String, countryCode: String): Boolean {
        return try {
            val validatePhoneNumber = phoneNumberUtil.parse(phoneNumber, countryCode.uppercase())
            val isValidNumber = phoneNumberUtil.isValidNumber(validatePhoneNumber)
            isValidNumber
        } catch (countryNotFound: Exception) {
            return false
        }
    }

    private suspend fun getCountryForISO(nameCode: String): CountryCode? {
        if (countryCodes.isEmpty()) {
            readCountriesFromXml()
        }
        return countryCodes.firstOrNull { it.nameCode.equals(nameCode, ignoreCase = true) }
    }

    private suspend fun readCountriesFromXml() {
        val countries = mutableListOf<CountryCode>()
        try {
            val xmlParserFactory = XmlPullParserFactory.newInstance()
            val xmlParser = xmlParserFactory.newPullParser()
            val inputStream = context.resources.openRawResource(R.raw.ccp_english)
            xmlParser.setInput(inputStream, "UTF-8")

            var event = xmlParser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                val name = xmlParser.name
                when {
                    event == XmlPullParser.END_TAG && name.equals(KEY_COUNTRY) -> {
                        val nameCode = xmlParser.getAttributeValue(null, KEY_NAME_CODE).uppercase()
                        val phoneCode = xmlParser.getAttributeValue(null, KEY_PHONE_CODE)
                        val name = xmlParser.getAttributeValue(null, KEY_NAME)
                        val flagEmoji = xmlParser.getAttributeValue(null, KEY_FLAG_EMOJI)
                        val mask = getMaskForCountryCode(nameCode, phoneCode)
                        countries.add(CountryCode(nameCode, phoneCode, name, flagEmoji, mask))
                    }
                }
                event = xmlParser.next()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while reading from XML file")
        }
        countryCodes.clear()
        countryCodes.addAll(countries)
    }

    private fun getMaskForCountryCode(countryCode: String, phoneCode: String): String {
        return try {
            val exampleNumber = phoneNumberUtil.getExampleNumber(countryCode)
            val internationalFormat =
                phoneNumberUtil.format(exampleNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
            internationalFormat.replace("+$phoneCode", emptyString())
        } catch (e: Exception) {
            countryCodeMask[countryCode].orEmpty()
        }
    }
}

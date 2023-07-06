package org.p2p.wallet.auth.gateway.parser

import android.content.res.Resources
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.utils.emptyString

private const val KEY_COUNTRY = "country"
private const val KEY_NAME_CODE = "name_code"
private const val KEY_NAME_CODE_ALPHA3 = "name_code_alpha3"
private const val KEY_PHONE_CODE = "phone_code"
private const val KEY_NAME = "name"
private const val KEY_FLAG_EMOJI = "flag_emoji"

class CountryCodeXmlParser(
    private val resources: Resources,
    private val phoneNumberUtil: PhoneNumberUtil
) {
    fun parserCountryCodesFromXmlFile(): List<CountryCode> = try {
        val resultCountries = mutableListOf<CountryCode>()
        val xmlParserFactory = XmlPullParserFactory.newInstance()
        val xmlParser = xmlParserFactory.newPullParser()
        val inputStream = resources.openRawResource(R.raw.ccp_english)
        xmlParser.setInput(inputStream, "UTF-8")

        var event = xmlParser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tagName = xmlParser.name
            when {
                event == XmlPullParser.START_TAG && tagName.equals(KEY_COUNTRY) -> {
                    val countryCodeAlpha2 = xmlParser.getAttributeValue(null, KEY_NAME_CODE).uppercase()
                    val countryCodeAlpha3 = xmlParser.getAttributeValue(null, KEY_NAME_CODE_ALPHA3).uppercase()
                    val phoneCode = xmlParser.getAttributeValue(null, KEY_PHONE_CODE)
                    val name = xmlParser.getAttributeValue(null, KEY_NAME)
                    val flagEmoji = xmlParser.getAttributeValue(null, KEY_FLAG_EMOJI)
                    val mask = getMaskForCountryCode(countryCodeAlpha2, phoneCode)
                    resultCountries.add(
                        CountryCode(
                            nameCodeAlpha2 = countryCodeAlpha2,
                            nameCodeAlpha3 = countryCodeAlpha3,
                            phoneCode = phoneCode,
                            countryName = name,
                            flagEmoji = flagEmoji,
                            mask = mask
                        )
                    )
                }
            }
            event = xmlParser.next()
        }
        resultCountries.sortedBy { it.countryName }
    } catch (error: Throwable) {
        Timber.e(error, "Error while reading from XML file")
        emptyList()
    }

    private fun getMaskForCountryCode(countryCode: String, phoneCode: String): String {
        return try {
            val exampleNumber: Phonenumber.PhoneNumber? = phoneNumberUtil.getExampleNumber(countryCode)
            if (exampleNumber != null) {
                val internationalFormat = phoneNumberUtil.format(
                    exampleNumber,
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                )
                internationalFormat.replace("+$phoneCode", emptyString())
            } else {
                emptyString()
            }
        } catch (e: Throwable) {
            Timber.i(e, "Get mask for country code failed")
            emptyString()
        }
    }

    fun isValidNumberForRegion(phoneNumber: String, region: String): Boolean {
        return try {
            val validatePhoneNumber = phoneNumberUtil.parse(phoneNumber, region.uppercase())
            phoneNumberUtil.isValidNumber(validatePhoneNumber)
        } catch (countryNotFound: Throwable) {
            Timber.i(countryNotFound, "Phone number validation failed")
            return false
        }
    }

    fun parsePhoneNumber(phoneNumber: String, regionIsoAlpha2: String): Phonenumber.PhoneNumber? {
        return try {
            phoneNumberUtil.parse(phoneNumber, regionIsoAlpha2.uppercase())
        } catch (countryNotFound: Throwable) {
            Timber.i(countryNotFound, "Phone number validation failed")
            null
        }
    }
}

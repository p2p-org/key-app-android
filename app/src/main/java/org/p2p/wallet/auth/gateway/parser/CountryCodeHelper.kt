package org.p2p.wallet.auth.gateway.parser

import android.content.res.Resources
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.utils.emptyString
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber

class CountryCodeHelper(
    private val resources: Resources,
    private val phoneNumberUtil: PhoneNumberUtil
) {

    private var countryCodeMask = mutableMapOf<String, String>()

    private val KEY_COUNTRY = "country"
    private val KEY_NAME_CODE = "name_code"
    private val KEY_PHONE_CODE = "phone_code"
    private val KEY_NAME = "name"
    private val KEY_FLAG_EMOJI = "flag_emoji"

    fun parserCountryCodesFromXmlFile(): List<CountryCode> {
        val countries = mutableListOf<CountryCode>()
        try {
            val xmlParserFactory = XmlPullParserFactory.newInstance()
            val xmlParser = xmlParserFactory.newPullParser()
            val inputStream = resources.openRawResource(R.raw.ccp_english)
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
        return countries.sortedBy { it.name }
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

    fun isValidNumberForRegion(phoneNumber: String, countryCode: String): Boolean {
        return try {
            val validatePhoneNumber = phoneNumberUtil.parse(phoneNumber, countryCode.uppercase())
            phoneNumberUtil.isValidNumber(validatePhoneNumber)
        } catch (countryNotFound: Exception) {
            return false
        }
    }
}

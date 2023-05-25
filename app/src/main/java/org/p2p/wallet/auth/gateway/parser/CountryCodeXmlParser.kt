package org.p2p.wallet.auth.gateway.parser

import androidx.browser.trusted.sharing.ShareTarget.FileFormField.KEY_NAME
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
            val name = xmlParser.name
            when {
                event == XmlPullParser.END_TAG && name.equals(KEY_COUNTRY) -> {
                    val nameCode = xmlParser.getAttributeValue(null, KEY_NAME_CODE).uppercase()
                    val phoneCode = xmlParser.getAttributeValue(null, KEY_PHONE_CODE)
                    val name = xmlParser.getAttributeValue(null, KEY_NAME)
                    val flagEmoji = xmlParser.getAttributeValue(null, KEY_FLAG_EMOJI)
                    val mask = getMaskForCountryCode(nameCode, phoneCode)
                    resultCountries.add(CountryCode(nameCode, phoneCode, name, flagEmoji, mask))
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
            val internationalFormat = phoneNumberUtil.format(
                exampleNumber,
                PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
            )
            internationalFormat.replace("+$phoneCode", emptyString())
        } catch (e: Throwable) {
            Timber.i(e, "Get mask for country code failed")
            emptyString()
        }
    }

    fun isValidNumberForRegion(phoneNumber: String, countryCode: String): Boolean {
        return try {
            val validatePhoneNumber = phoneNumberUtil.parse(phoneNumber, countryCode.uppercase())
            phoneNumberUtil.isValidNumber(validatePhoneNumber)
        } catch (countryNotFound: Throwable) {
            Timber.i(countryNotFound, "Phone number validation failed")
            return false
        }
    }
}

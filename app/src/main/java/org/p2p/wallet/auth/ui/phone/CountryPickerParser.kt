package org.p2p.wallet.auth.ui.phone

import android.content.res.Resources
import kotlinx.coroutines.withContext
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

class CountryPickerParser(
    private val resources: Resources,
    private val dispatchers: CoroutineDispatchers
) {

    private val KEY_COUNTRY = "country"
    private val KEY_NAME_CODE = "name_code"
    private val KEY_PHONE_CODE = "phone_code"
    private val KEY_NAME = "name"
    private val KEY_FLAG_EMOJI = "flag_emoji"

    suspend fun readCountriesFromXml(): List<CountryCode> = withContext(dispatchers.io) {
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
                        countries.add(CountryCode(nameCode, phoneCode, name, flagEmoji))
                    }
                }
                event = xmlParser.next()
            }
        } catch (e: Exception) {
            Timber.e("Error while reading from XML file $e ")
        }
        return@withContext countries
    }

    /**
     * We have file phone_masks.txt with format countryCode : phone mask
     * read input stream, and split string by : delimiter
     * return map of country code - phone mask
     */
    fun readCountriesMasks(): Map<String, String> {
        val countryMasks = mutableMapOf<String, String>()
        try {
            val inputStream = resources.openRawResource(R.raw.phone_masks)
            val streamReader = InputStreamReader(inputStream)
            val reader = BufferedReader(streamReader)
            var line: String
            do {
                line = reader.readLine()
                if (line == null) {
                    break
                }
                val args = line.split(":")
                val countryCode = args[0]
                val countryMask = args[1]
                countryMasks[countryCode] = countryMask
            } while (line.isNotEmpty())
        } catch (e: Exception) {
            Timber.e("Error while reading from assets $e")
        }
        return countryMasks
    }

    fun getRawMaskHint(mask: String): String {
        val hint = StringBuilder()
        mask.forEach { char ->
            if (char !in "[]()-") {
                hint.append(char)
            }
        }
        return mask.replace(" ", "")
    }
}

package org.p2p.wallet.auth.ui.phone

import android.content.res.Resources
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

object CountryPickerParsingManager {

    fun readCountriesFromXml(resources: Resources): List<CountryCode> {
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
                    event == XmlPullParser.END_TAG && name.equals("country") -> {
                        val nameCode = xmlParser.getAttributeValue(null, "name_code").uppercase()
                        val phoneCode = xmlParser.getAttributeValue(null, "phone_code")
                        val name = xmlParser.getAttributeValue(null, "name")
                        val flagEmoji = xmlParser.getAttributeValue(null, "flag_emoji")
                        countries.add(CountryCode(nameCode, phoneCode, name, flagEmoji))
                    }
                }
                event = xmlParser.next()
            }
        } catch (e: Exception) {
            Timber.e("Error while reading from XML file ")
        }
        return countries
    }

    fun readCountriesMasks(resources: Resources): Map<String, String> {
        val countryMasks = hashMapOf<String, String>()
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
                val countryMask = args[1].replace("#", "0")
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

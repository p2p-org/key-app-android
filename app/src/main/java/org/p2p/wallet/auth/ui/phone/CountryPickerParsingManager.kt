package org.p2p.wallet.auth.ui.phone

import android.content.res.Resources
import android.util.SparseArray
import org.p2p.wallet.R
import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.auth.ui.phone.model.SameCountriesCode
import org.p2p.wallet.auth.ui.phone.model.SameCountriesCodeGroup
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber

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

    fun getSameCountriesCodeGroup(): SameCountriesCodeGroup {
        val groups = SparseArray<SameCountriesCode>()
        // Init [+358] country code
        val first = HashMap<String, String>()
        first["ax"] = "18"
        groups.put(358, SameCountriesCode("fi", 2, first))

        // Init [+44] country code

        val second = HashMap<String, String>()
        second["gg"] = "1481"
        second["im"] = "1624"
        second["je"] = "1534"
        groups.put(44, SameCountriesCode("gb", 4, second))

        // Init [+1]
        val third = HashMap<String, String>()
        third["ag"] = "268" // ANTIGUA_AND_BARBUDA_AREA_CODES
        third["ai"] = "264" // ANGUILLA_AREA_CODES
        third["as"] = "684" // American Samoa
        third["bb"] = "246" // BARBADOS_AREA_CODES
        third["bm"] = "441" // BERMUDA_AREA_CODES
        third["bs"] = "242" // BAHAMAS_AREA_CODES
        third["ca"] =
            "204/226/236/249/250/289/306/343/365/403/416/418/431/437/438/450/506/514/519/579/581/587/600/601/604/613/639/647/705/709/769/778/780/782/807/819/825/867/873/902/905/" // CANADA_AREA_CODES
        third["dm"] = "767" // DOMINICA_AREA_CODES
        third["do"] = "809/829/849" // DOMINICAN_REPUBLIC_AREA_CODES
        third["gd"] = "473" // GRENADA_AREA_CODES
        third["gu"] = "671" // Guam
        third["jm"] = "876" // JAMAICA_AREA_CODES
        third["kn"] = "869" // SAINT_KITTS_AND_NEVIS_AREA_CODES
        third["ky"] = "345" // CAYMAN_ISLANDS_AREA_CODES
        third["lc"] = "758" // SAINT_LUCIA_AREA_CODES
        third["mp"] = "670" // Northern Mariana Islands
        third["ms"] = "664" // MONTSERRAT_AREA_CODES
        third["pr"] = "787" // PUERTO_RICO_AREA_CODES
        third["sx"] = "721" // SINT_MAARTEN_AREA_CODES
        third["tc"] = "649" // TURKS_AND_CAICOS_ISLANDS_AREA_CODES
        third["tt"] = "868" // TRINIDAD_AND_TOBAGO_AREA_CODES
        third["vc"] = "784" // SAINT_VINCENT_AND_THE_GRENADINES_AREA_CODES
        third["vg"] = "284" // BRITISH_VIRGIN_ISLANDS_AREA_CODES
        third["vi"] = "340" // US_VIRGIN_ISLANDS_AREA_CODES
        groups.put(1, SameCountriesCode("us", 3, third))

        return SameCountriesCodeGroup(groups)
    }
}

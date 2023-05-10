package org.p2p.wallet.moonpay.extensions

object MoonpayConstants {
    object CountryAlpha3Code {
        const val UK = "GBR"
        const val US = "USA"
        val EU = setOf(
            "AND",
            "AUT",
            "BEL",
            "BGR",
            "HRV",
            "CYP",
            "CZE",
            "DNK",
            "EST",
            "FIN",
            "FRA",
            "DEU",
            "GIB",
            "GRC",
            "HUN",
            "ISL",
            "IRL",
            "ITA",
            "LVA",
            "LIE",
            "LTU",
            "LUX",
            "MLT",
            "MCO",
            "NLD",
            "NOR",
            "POL",
            "PRT",
            "ROU",
            "SMR",
            "SVK",
            "SVN",
            "ESP",
            "SWE",
            "CHE",
            "VAT",
        )

        fun isCountryInEu(countryAlpha3: String): Boolean = countryAlpha3 in EU
    }
}

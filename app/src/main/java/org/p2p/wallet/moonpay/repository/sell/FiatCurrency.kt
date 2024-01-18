package org.p2p.wallet.moonpay.repository.sell

import org.p2p.wallet.moonpay.extensions.MoonpayConstants.CountryAlpha3Code

enum class FiatCurrency(val abbreviation: String, val uiSymbol: String) {
    EUR(abbreviation = "eur", uiSymbol = "€"),
    USD(abbreviation = "usd", uiSymbol = "$"),
    GBP(abbreviation = "gbp", uiSymbol = "£");

    companion object {
        fun getFromAbbreviation(abbreviation: String): FiatCurrency {
            return values().firstOrNull { it.abbreviation == abbreviation } ?: USD
        }

        fun getFromAlpha3(countryAlpha3: String): FiatCurrency {
            return when (countryAlpha3) {
                CountryAlpha3Code.US -> USD
                CountryAlpha3Code.UK -> GBP
                else -> EUR // if it's not US or UK, then it's EU
            }
        }
    }
}

package org.p2p.wallet.moonpay.repository.sell

import org.p2p.wallet.moonpay.extensions.MoonpayConstants.CountryAlpha3Code

enum class FiatCurrency(val abbriviation: String, val uiSymbol: String) {
    EUR(abbriviation = "eur", uiSymbol = "€"),
    USD(abbriviation = "usd", uiSymbol = "$"),
    GBP(abbriviation = "gbp", uiSymbol = "£");

    companion object {
        fun getFromAbbreviation(abbreviation: String): FiatCurrency {
            return values().firstOrNull { it.abbriviation == abbreviation } ?: USD
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

package org.p2p.wallet.moonpay.repository.sell

import org.p2p.wallet.moonpay.extensions.MoonpayConstants.CountryAlpha3Code

enum class SellTransactionFiatCurrency(val abbriviation: String, val uiSymbol: String) {
    EUR(abbriviation = "eur", uiSymbol = "€"),
    USD(abbriviation = "usd", uiSymbol = "$"),
    GBP(abbriviation = "gbp", uiSymbol = "£");

    companion object {
        fun getFromCountryAbbreviation(countryAlpha3: String): SellTransactionFiatCurrency {
            return when (countryAlpha3) {
                CountryAlpha3Code.US -> USD
                CountryAlpha3Code.UK -> GBP
                else -> EUR // if it's not US or UK, then it's EU
            }
        }
    }
}

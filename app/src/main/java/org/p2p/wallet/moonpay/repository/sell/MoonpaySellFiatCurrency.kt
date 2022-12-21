package org.p2p.wallet.moonpay.repository.sell

enum class MoonpaySellFiatCurrency(val symbol: String, val uiSymbol: String) {
    EUR("eur", "EUR"),
    USD("usd", "$"),
    GBP("gbp", "GBP");

    companion object {
        fun getFromCountryAbbreviation(abbreviation: String): MoonpaySellFiatCurrency {
            return when (abbreviation) {
                "US" -> USD
                "UK" -> GBP
                "EU" -> EUR
                else -> EUR // if it's not US or UK, then it's EU
            }
        }
    }
}

package org.p2p.wallet.moonpay.repository.sell

enum class MoonpaySellFiatCurrency(val symbol: String, val currencySymbol: String) {
    EUR("eur", "€"),
    USD("usd", "$"),
    GBP("gbp", "GBP");

    companion object {
        fun getFromCountryAbbreviation(abbreviation: String): MoonpaySellFiatCurrency {
            return when (abbreviation) {
                "UK" -> GBP
                "EU" -> EUR
                else -> USD
            }
        }
    }
}

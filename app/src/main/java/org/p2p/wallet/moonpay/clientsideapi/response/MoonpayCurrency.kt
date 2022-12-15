package org.p2p.wallet.moonpay.clientsideapi.response

sealed class MoonpayCurrency {
    abstract val currencyId: String
    abstract val amounts: MoonpayCurrencyAmounts

    data class CryptoToken(
        val tokenSymbol: String,
        val tokenName: String,
        override val currencyId: String,
        override val amounts: MoonpayCurrencyAmounts
    ) : MoonpayCurrency()

    data class Fiat(
        val fiatCode: String,
        val fiatName: String,
        override val currencyId: String,
        override val amounts: MoonpayCurrencyAmounts
    ) : MoonpayCurrency()
}

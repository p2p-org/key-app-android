package org.p2p.wallet.moonpay.clientsideapi.response

import org.p2p.core.utils.Constants

sealed class MoonpayCurrency {
    abstract val currencyId: String
    abstract val amounts: MoonpayCurrencyAmounts

    // it's okay to keep equals by symbol here
    // because we only have non-scam wrapped SOL in/out Moonpay
    fun isSol() = this is CryptoToken && tokenSymbol.equals(Constants.SOL_SYMBOL, ignoreCase = true)

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

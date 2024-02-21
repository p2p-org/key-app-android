package org.p2p.wallet.moonpay.clientsideapi.response

import org.p2p.core.crypto.Base58String
import org.p2p.core.utils.Constants

sealed class MoonpayCurrency {
    abstract val currencyId: String
    abstract val amounts: MoonpayCurrencyAmounts

    fun isSol() = this is CryptoToken && tokenMint == Constants.WRAPPED_SOL_MINT_B58

    data class CryptoToken(
        val tokenSymbol: String,
        val tokenName: String,
        val tokenMint: Base58String,
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

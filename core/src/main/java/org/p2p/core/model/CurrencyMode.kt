package org.p2p.core.model

import org.p2p.core.utils.Constants
import org.p2p.core.utils.Constants.FIAT_FRACTION_LENGTH
import org.p2p.core.token.Token as UserToken

sealed interface CurrencyMode {

    val fractionLength: Int

    data class Token constructor(val symbol: String, override val fractionLength: Int) : CurrencyMode {
        constructor(token: UserToken) : this(token.tokenSymbol, token.decimals)
    }

    sealed class Fiat(val fiatSymbol: String, val fiatAbbreviation: String) : CurrencyMode {
        override val fractionLength = FIAT_FRACTION_LENGTH

        object Usd : Fiat("$", Constants.USD_READABLE_SYMBOL)
        object Eur : Fiat("€", Constants.EUR_SYMBOL)
        object Gbp : Fiat("£", Constants.GBP_SYMBOL)
    }

    fun getTypedSymbol(): String = when (this) {
        is Token -> symbol
        is Fiat -> fiatAbbreviation
    }

    fun isFiat(): Boolean = this is Fiat
}

package org.p2p.core.model

import org.p2p.core.utils.Constants
import org.p2p.core.utils.Constants.FIAT_FRACTION_LENGTH
import org.p2p.core.token.Token as UserToken

sealed interface CurrencyMode {
    data class Token constructor(val symbol: String, val fractionLength: Int) : CurrencyMode {
        constructor(token: UserToken) : this(token.tokenSymbol, token.decimals)
    }

    sealed class Fiat(val fiatSymbol: String, val fiatAbbreviation: String) : CurrencyMode {
        val fractionLength = FIAT_FRACTION_LENGTH
        object Usd : Fiat("$", Constants.USD_READABLE_SYMBOL)
        object Eur : Fiat("€", Constants.EUR_SYMBOL)
        object Gbp : Fiat("£", Constants.GBP_SYMBOL)
    }
}

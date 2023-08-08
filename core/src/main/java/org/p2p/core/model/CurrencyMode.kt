package org.p2p.core.model

import org.p2p.core.utils.Constants.EUR_SYMBOL
import org.p2p.core.utils.Constants.FIAT_FRACTION_LENGTH
import org.p2p.core.utils.Constants.GBP_SYMBOL
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.token.Token as UserToken

sealed interface CurrencyMode {

    val fractionLength: Int

    data class Token constructor(val symbol: String, override val fractionLength: Int) : CurrencyMode {
        constructor(token: UserToken) : this(token.tokenSymbol, token.decimals)
    }

    sealed class Fiat(val fiatSymbol: String, val fiatAbbreviation: String) : CurrencyMode {
        override val fractionLength = FIAT_FRACTION_LENGTH

        object Usd : Fiat("$", USD_READABLE_SYMBOL)
        object Eur : Fiat("€", EUR_SYMBOL)
        object Gbp : Fiat("£", GBP_SYMBOL)
    }

    fun getTypedSymbol(): String = when (this) {
        is Token -> symbol
        is Fiat -> fiatAbbreviation
    }

    fun isFiat(): Boolean = this is Fiat

    fun toggle(currentToken: UserToken): CurrencyMode = when (this) {
        is Fiat -> Token(currentToken)
        is Token -> Fiat.Usd
    }

    fun getInputSymbols(currentToken: UserToken): Pair<String, String> {
        return when (val mode = this) {
            is Token -> currentToken.tokenSymbol to Fiat.Usd.fiatAbbreviation
            is Fiat -> mode.fiatAbbreviation to currentToken.tokenSymbol
        }
    }
}

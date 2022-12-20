package org.p2p.wallet.send.model

import org.p2p.core.token.Token as UserToken

sealed class CurrencyMode {
    data class Token constructor(val symbol: String, val fractionLength: Int) : CurrencyMode() {
        constructor(token: UserToken) : this(token.tokenSymbol, token.decimals)
    }

    object Usd : CurrencyMode()
}

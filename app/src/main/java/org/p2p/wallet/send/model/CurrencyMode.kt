package org.p2p.wallet.send.model

sealed class CurrencyMode {
    data class Token(val symbol: String) : CurrencyMode()
    object Usd : CurrencyMode()
}

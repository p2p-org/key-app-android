package org.p2p.wallet.main.model

sealed class CurrencyMode {
    data class Token(val symbol: String) : CurrencyMode()
    object Usd : CurrencyMode()
}
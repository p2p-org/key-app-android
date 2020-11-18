package com.wowlet.entities.local

data class WalletItem(
    val tokenSymbol: String = "",
    val mintAddress: String = "",
    val tokenName: String = "",
    val icon: String = "",
    var price: Double = 0.0,
    var tkns: Double = 0.0
)
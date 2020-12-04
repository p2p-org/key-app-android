package com.wowlet.entities.local

data class WalletItem(
    val tokenSymbol: String = "",
    val depositAddress: String = "",
    val decimals: Int = 0,
    val mintAddress: String = "",
    val tokenName: String = "",
    val icon: String = "",
    var price: Double = 0.0,
    var amount: Double = 0.0
)
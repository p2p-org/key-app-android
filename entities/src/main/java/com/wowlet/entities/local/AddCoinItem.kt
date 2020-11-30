package com.wowlet.entities.local

data class AddCoinItem(
    val tokenSymbol: String,
    val mintAddress: String,
    val tokenName: String,
    val icon: String,
    val change24hPrice: Double,
    val change24hPercentages: Double,
    var isShowMindAddress: Boolean=false,
)
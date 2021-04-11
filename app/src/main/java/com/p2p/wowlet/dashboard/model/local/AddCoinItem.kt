package com.p2p.wowlet.dashboard.model.local

data class AddCoinItem(
    val tokenSymbol: String,
    val mintAddress: String,
    val tokenName: String,
    val icon: String,
    val change24hPrice: Double,
    val change24hPercentages: Double,
    val isChange24hPercentagesPositive: Boolean = (change24hPercentages >= 0.0),
    val currency: Double,
    var minBalance: Double? = null,
    var walletAddress: String? = null,
    var isShowMindAddress: Boolean = false,
    var isAlreadyAdded: Boolean = false,
    var navigatingBack: Boolean = false
)
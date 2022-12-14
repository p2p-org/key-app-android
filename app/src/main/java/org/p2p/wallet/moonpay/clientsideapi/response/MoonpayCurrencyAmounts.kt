package org.p2p.wallet.moonpay.clientsideapi.response

data class MoonpayCurrencyAmounts(
    val minAmount: Double,
    val maxAmount: Double,
    val minBuyAmount: Double,
    val maxBuyAmount: Double,
    val minSellAmount: Double,
    val maxSellAmount: Double
)

package org.p2p.wallet.moonpay.clientsideapi.response

import java.math.BigDecimal

data class MoonpayCurrencyAmounts(
    val minAmount: BigDecimal,
    val maxAmount: BigDecimal,
    val minBuyAmount: BigDecimal,
    val maxBuyAmount: BigDecimal,
    val minSellAmount: BigDecimal,
    val maxSellAmount: BigDecimal
)

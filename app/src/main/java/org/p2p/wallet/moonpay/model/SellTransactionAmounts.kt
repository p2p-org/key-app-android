package org.p2p.wallet.moonpay.model

import java.math.BigDecimal

class SellTransactionAmounts(
    val tokenAmount: BigDecimal,
    val feeAmount: BigDecimal,
    val usdAmount: BigDecimal,
    val eurAmount: BigDecimal,
    val gbpAmount: BigDecimal
)

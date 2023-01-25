package org.p2p.wallet.moonpay.model

import java.math.BigDecimal

class SellTransactionAmounts(
    val tokenAmount: BigDecimal,
    val feeAmount: BigDecimal,
    val usdRate: BigDecimal,
    val eurRate: BigDecimal,
    val gbpRate: BigDecimal,
    val amountInFiat: BigDecimal
)

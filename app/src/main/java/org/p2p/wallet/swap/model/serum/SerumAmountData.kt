package org.p2p.wallet.swap.model.serum

import java.math.BigDecimal

data class SerumAmountData(
    val destinationAmount: String,
    val estimatedReceiveAmount: BigDecimal
)

package com.p2p.wallet.swap.serum.model

import java.math.BigDecimal

data class SerumAmountData(
    val destinationAmount: String,
    val estimatedReceiveAmount: BigDecimal
)
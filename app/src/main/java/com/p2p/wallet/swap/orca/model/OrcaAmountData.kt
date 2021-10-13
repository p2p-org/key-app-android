package com.p2p.wallet.swap.orca.model

import java.math.BigDecimal

data class OrcaAmountData(
    val estimatedDestinationAmount: String,
    val minReceiveAmount: BigDecimal
)
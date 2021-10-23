package org.p2p.wallet.swap.model.orca

import java.math.BigDecimal

data class OrcaAmountData(
    val estimatedDestinationAmount: String,
    val minReceiveAmount: BigDecimal
)
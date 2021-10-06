package com.p2p.wallet.swap.orca.model

import java.math.BigDecimal

data class OrcaAmountData(
    val destinationAmount: String,
    val estimatedReceiveAmount: BigDecimal,
    val estimatedReceiveSymbol: String
)
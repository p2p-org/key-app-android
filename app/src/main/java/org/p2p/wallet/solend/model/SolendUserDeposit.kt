package org.p2p.wallet.solend.model

import java.math.BigDecimal

data class SolendUserDeposit(
    val depositedAmount: BigDecimal,
    val depositTokenSymbol: String
)

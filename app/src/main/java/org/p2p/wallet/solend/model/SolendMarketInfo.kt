package org.p2p.wallet.solend.model

import java.math.BigDecimal
import java.math.BigInteger

data class SolendMarketInfo(
    val tokenSymbol: String,
    val currentSupply: BigDecimal,
    val depositLimitAmount: BigInteger,
    val supplyInterest: BigDecimal
)

package org.p2p.wallet.solend.model

import java.math.BigInteger

data class SolendDepositFee(
    val accountCreationFee: BigInteger,
    val rent: BigInteger
)

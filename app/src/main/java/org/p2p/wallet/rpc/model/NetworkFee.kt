package org.p2p.wallet.rpc.model

import java.math.BigInteger

data class NetworkFee(
    val lamportsPerSignature: BigInteger,
    val minBalanceForRentExemption: BigInteger
)

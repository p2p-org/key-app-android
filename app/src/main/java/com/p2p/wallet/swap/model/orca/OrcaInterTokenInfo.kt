package com.p2p.wallet.swap.model.orca

import java.math.BigInteger

data class OrcaInterTokenInfo(
    val tokenName: String,
    val outputAmount: BigInteger?,
    val minAmountOut: BigInteger?,
    val isStableSwap: Boolean
)
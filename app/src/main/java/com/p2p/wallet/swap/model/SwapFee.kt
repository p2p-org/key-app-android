package com.p2p.wallet.swap.model

import java.math.BigInteger

data class SwapFee(
    val tokenSymbol: String,
    val lamports: BigInteger,
    val stringValue: String? = null
) {

    constructor(stringValue: String) : this(
        tokenSymbol = "", lamports = BigInteger.ZERO, stringValue = stringValue
    )
}
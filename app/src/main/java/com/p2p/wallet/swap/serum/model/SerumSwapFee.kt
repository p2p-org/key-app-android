package com.p2p.wallet.swap.serum.model

import java.math.BigInteger

data class SerumSwapFee(
    val tokenSymbol: String,
    val lamports: BigInteger,
    val stringValue: String? = null
) {

    constructor(stringValue: String) : this(
        tokenSymbol = "", lamports = BigInteger.ZERO, stringValue = stringValue
    )
}
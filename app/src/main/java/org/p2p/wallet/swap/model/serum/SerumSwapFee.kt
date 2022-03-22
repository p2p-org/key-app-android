package org.p2p.wallet.swap.model.serum

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

package org.p2p.wallet.utils

import java.math.BigDecimal
import java.math.BigInteger

data class AmountInLamports(val amount: BigInteger) {
    constructor(amountDecimal: BigDecimal, decimals: Int) : this(amountDecimal.toLamports(decimals))

    operator fun compareTo(totalSwapFee: AmountInLamports): Int = amount.compareTo(totalSwapFee.amount)
    operator fun plus(enteredSourceAmount: AmountInLamports): AmountInLamports {
        return AmountInLamports(amount + enteredSourceAmount.amount)
    }
}

fun AmountInLamports?.orZeroLamports(): AmountInLamports {
    return AmountInLamports(BigInteger.ZERO)
}

fun BigDecimal.toLamportsValue(decimals: Int): AmountInLamports {
    return AmountInLamports(this, decimals)
}

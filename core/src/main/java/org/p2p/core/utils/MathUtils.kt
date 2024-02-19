package org.p2p.core.utils

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

private const val DEFAULT_DECIMALS_VALUE = 9

// scales by default to the decimals
fun BigInteger.divideSafe(value: BigInteger): BigInteger =
    if (value.isZero()) BigInteger.ZERO
    else this / value

// scales by default to the decimals
fun BigDecimal.divideSafe(
    value: BigDecimal,
    decimals: Int = DEFAULT_DECIMALS_VALUE,
    roundingMode: RoundingMode = RoundingMode.HALF_EVEN
): BigDecimal =
    if (this.isZero() || value.isZero()) {
        BigDecimal.ZERO
    } else {
        this.divide(value, decimals, roundingMode).stripTrailingZeros()
    }

fun clamp(value: Int, min: Int, max: Int): Int = min(max(min, value), max)

/**
 * @param ratio - ratio in range (0.0 .. 1.0)
 */
fun BigDecimal.subtractRatio(ratio: BigDecimal): BigDecimal {
    require(ratio.toDouble() in 0.0..1.0) {
        "Expected ratio between 0.0 and 1.0, but given is $ratio"
    }
    return this.subtract(this.multiply(ratio))
}

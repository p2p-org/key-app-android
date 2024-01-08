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
fun BigDecimal.divideSafe(value: BigDecimal, decimals: Int = DEFAULT_DECIMALS_VALUE): BigDecimal =
    if (this.isZero() || value.isZero()) BigDecimal.ZERO
    else this.divide(value, decimals, RoundingMode.HALF_EVEN).stripTrailingZeros()

fun clamp(value: Int, min: Int, max: Int): Int = min(max(min, value), max)

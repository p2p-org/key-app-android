package org.p2p.wallet.utils

import org.p2p.core.utils.isZero
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

private const val DEFAULT_DECIMALS_VALUE = 9

fun BigInteger.divideSafe(value: BigInteger): BigInteger =
    if (value.isZero()) BigInteger.ZERO
    else this / value

fun BigDecimal.divideSafe(value: BigDecimal, decimals: Int = DEFAULT_DECIMALS_VALUE): BigDecimal =
    if (this.isZero() || value.isZero()) BigDecimal.ZERO
    else this.divide(value, decimals, RoundingMode.HALF_EVEN).stripTrailingZeros()

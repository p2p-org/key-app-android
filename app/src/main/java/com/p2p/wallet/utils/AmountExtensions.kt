package com.p2p.wallet.utils

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.pow

private const val ZERO_VALUE = 0.0
private const val POWER_VALUE = 10.0
private const val DEFAULT_DECIMAL = 9

private const val SCALE_VALUE_SHORT = 2
private const val SCALE_VALUE_MEDIUM = 6
private const val SCALE_VALUE_LONG = 9

fun String.toBigDecimalOrZero(): BigDecimal {
    val removedZeros = this.replace("(?<=\\d)\\.?0+(?![\\d\\.])", "")
    return removedZeros.toBigDecimalOrNull() ?: BigDecimal.ZERO
}

fun Int.toPowerValue(): BigDecimal =
    BigDecimal(POWER_VALUE.pow(this))

fun BigDecimal.scaleShort(): BigDecimal =
    this.setScale(SCALE_VALUE_SHORT, RoundingMode.HALF_EVEN)
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.2
        .toPlainString() // correcting non-decimal values which stripped, 1+E3 -> 1000
        .toBigDecimal()

fun BigDecimal.scaleMedium(): BigDecimal =
    if (this.isZero()) this else this.setScale(SCALE_VALUE_MEDIUM, RoundingMode.HALF_EVEN)
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.2
        .toPlainString() // correcting non-decimal values which stripped, 1+E3 -> 1000
        .toBigDecimal()

fun BigDecimal.scaleLong(): BigDecimal =
    if (this.isZero()) this else this.setScale(SCALE_VALUE_LONG, RoundingMode.HALF_EVEN)
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.2
        .toPlainString() // correcting non-decimal values which stripped, 1+E3 -> 1000
        .toBigDecimal()

fun BigInteger.fromLamports(decimals: Int = DEFAULT_DECIMAL): BigDecimal =
    BigDecimal(this.toDouble() / (POWER_VALUE.pow(decimals)))
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.2
        .toPlainString() // correcting non-decimal values which stripped, 1+E3 -> 1000
        .toBigDecimal()

fun BigDecimal.toLamports(decimals: Int): BigInteger =
    this.multiply(decimals.toPowerValue()).toBigInteger()

fun BigDecimal.isZero() = this.compareTo(BigDecimal.ZERO) == 0
fun BigDecimal.isNotZero() = this.compareTo(BigDecimal.ZERO) != 0
fun BigDecimal.isMoreThan(value: BigDecimal) = this.compareTo(value) == 1
fun BigDecimal.isLessThan(value: BigDecimal) = this.compareTo(value) == -1

fun Double.isZero() = this == ZERO_VALUE
fun BigInteger.isZero() = this.compareTo(BigInteger.ZERO) == 0
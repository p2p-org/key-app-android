package com.p2p.wallet.amount

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.pow

private const val POWER_VALUE = 10.0
private const val DEFAULT_DECIMAL = 9

private const val SCALE_VALUE_SHORT = 2
private const val SCALE_VALUE_MEDIUM = 6
private const val SCALE_VALUE_LONG = 9

fun String.toBigDecimalOrZero(): BigDecimal =
    this.toBigDecimalOrNull()?.stripTrailingZeros() ?: BigDecimal.ZERO

fun Double?.valueOrZero(): BigDecimal = BigDecimal(this ?: 0.0)

fun Int.toPowerValue(): BigDecimal =
    BigDecimal(POWER_VALUE.pow(this))

fun BigDecimal.scaleShort(): BigDecimal =
    this.setScale(SCALE_VALUE_SHORT, RoundingMode.HALF_EVEN).stripTrailingZeros()

fun BigDecimal.scaleAmount(): BigDecimal =
    if (this.isZero()) this else this.setScale(SCALE_VALUE_MEDIUM, RoundingMode.HALF_EVEN).stripTrailingZeros()

fun BigDecimal.scalePrice(): BigDecimal =
    if (this.isZero()) this else this.setScale(SCALE_VALUE_LONG, RoundingMode.HALF_EVEN).stripTrailingZeros()

fun BigInteger.fromLamports(decimals: Int = DEFAULT_DECIMAL): BigDecimal =
    BigDecimal(this.toDouble() / (POWER_VALUE.pow(decimals))).stripTrailingZeros()

fun BigDecimal.toLamports(decimals: Int): BigInteger =
    this.multiply(decimals.toPowerValue()).toBigInteger()

fun BigDecimal.isZero() = this.compareTo(BigDecimal.ZERO) == 0
fun BigDecimal.isMoreThan(value: BigDecimal) = this.compareTo(value) == 1
fun BigDecimal.isLessThan(value: BigDecimal) = this.compareTo(value) == -1
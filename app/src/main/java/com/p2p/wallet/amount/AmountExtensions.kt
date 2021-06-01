package com.p2p.wallet.amount

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.pow

private const val POWER_VALUE = 10.0
private const val DEFAULT_DECIMAL = 9

private const val SCALE_VALUE_SHORT = 2
private const val SCALE_VALUE = 6
private const val SCALE_VALUE_PRICE = 9

private const val DOUBLE_ZERO_VALUE = 0.0

fun String.toBigDecimalOrZero() = this.toBigDecimalOrNull() ?: BigDecimal.ZERO

fun Double?.valueOrZero() = this ?: DOUBLE_ZERO_VALUE

fun Double.validatedValue() = if (this.isInfinite() || this.isNaN()) DOUBLE_ZERO_VALUE else this

fun Int.toPowerValue(): BigDecimal =
    POWER_VALUE.pow(this).toBigDecimal()

fun BigDecimal.scaleShort(): BigDecimal =
    this.setScale(SCALE_VALUE_SHORT, RoundingMode.HALF_EVEN)

fun BigDecimal.scaleAmount(): BigDecimal =
    this.setScale(SCALE_VALUE, RoundingMode.HALF_UP)

fun BigDecimal.scalePrice(): BigDecimal =
    this.setScale(SCALE_VALUE_PRICE, RoundingMode.HALF_UP)

fun Long.fromLamports(decimals: Int = DEFAULT_DECIMAL) = BigDecimal(this.toDouble() / (POWER_VALUE.pow(decimals)))

fun BigInteger.fromLamports(decimals: Int = DEFAULT_DECIMAL) = BigDecimal(this.toDouble() / (POWER_VALUE.pow(decimals)))

fun BigDecimal.toLamports(decimals: Int) = this.multiply(decimals.toPowerValue()).toBigInteger()
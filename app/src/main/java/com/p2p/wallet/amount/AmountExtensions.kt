package com.p2p.wallet.amount

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.pow

private const val POWER_VALUE = 10.0
private const val SCALE_VALUE = 6

private const val DOUBLE_ZERO_VALUE = 0.0

fun Double?.valueOrZero() = this ?: DOUBLE_ZERO_VALUE

fun Double.toPowerValue() =
    POWER_VALUE.pow(this).toBigDecimal()

fun Int.toPowerValue(): BigInteger =
    POWER_VALUE.pow(this).toBigDecimal().toBigInteger()

fun BigDecimal.divideRounded(value: BigDecimal): BigDecimal =
    this.divide(value, RoundingMode.HALF_EVEN)

fun BigDecimal.scaleDefault(): BigDecimal =
    this.setScale(SCALE_VALUE, RoundingMode.HALF_EVEN)
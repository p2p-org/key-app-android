package com.p2p.wallet.amount

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

private const val POWER_VALUE = 10.0
private const val SCALE_VALUE = 6

fun Double.toDecimalValue() =
    POWER_VALUE.pow(this).toBigDecimal()

fun Int.toDecimalValue() =
    POWER_VALUE.pow(this).toBigDecimal()

fun BigDecimal.divideRounded(value: BigDecimal): BigDecimal =
    this.divide(value, RoundingMode.HALF_EVEN)

fun BigDecimal.scaleDefault(): BigDecimal =
    this.setScale(SCALE_VALUE, RoundingMode.HALF_EVEN)
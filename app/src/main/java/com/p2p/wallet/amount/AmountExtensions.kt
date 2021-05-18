package com.p2p.wallet.amount

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.pow

private const val POWER_VALUE = 10.0

fun Double.toDecimalValue() =
    POWER_VALUE.pow(this).toBigDecimal()

fun Int.toDecimalValue() =
    POWER_VALUE.pow(this).toBigDecimal()

fun BigDecimal.toDecimalValue() =
    POWER_VALUE.pow(this.toDouble()).toBigDecimal()
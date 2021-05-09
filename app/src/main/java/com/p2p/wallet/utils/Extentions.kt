package com.p2p.wallet.utils

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.roundCurrencyValue(): Double {
    return BigDecimal(this).setScale(2, RoundingMode.HALF_EVEN).toDouble()
}

fun Double.roundToMilCurrencyValue(): BigDecimal {
    return BigDecimal(this).setScale(6, RoundingMode.HALF_UP)
}

fun Double.roundToBilCurrencyValue(): BigDecimal {
    return BigDecimal(this).setScale(9, RoundingMode.HALF_UP)
}
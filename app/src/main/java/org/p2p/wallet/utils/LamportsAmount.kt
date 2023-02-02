package org.p2p.wallet.utils

import java.math.BigDecimal

data class LamportsAmount(val value: BigDecimal) {
    val valueAsDouble: Double
        get() = value.toDouble()
}

fun BigDecimal.toLamportsInstance(): LamportsAmount = LamportsAmount(this)

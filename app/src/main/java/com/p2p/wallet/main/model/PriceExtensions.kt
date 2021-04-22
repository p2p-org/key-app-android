package com.p2p.wallet.main.model

import java.lang.ref.SoftReference
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

val MILLION = 1_000_000.toBigDecimal()

private var decimalFormatProvider = SoftReference(mutableMapOf<Int, DecimalFormat>())

fun Int.provideDecimalWithScale(): DecimalFormat {
    val map: MutableMap<Int, DecimalFormat> = if (decimalFormatProvider.get() == null) {
        mutableMapOf<Int, DecimalFormat>().also { decimalFormatProvider = SoftReference(it) }
    } else {
        decimalFormatProvider.get()!!
    }

    return map.getOrPut(this) {
        DecimalFormat("#,##0." + (0 until this).joinToString(separator = "") { "0" }).also {
            it.roundingMode = RoundingMode.FLOOR
            it.isDecimalSeparatorAlwaysShown = true
        }
    }
}

fun BigDecimal.roundAndToFloat() = setScale(2, RoundingMode.HALF_EVEN).toFloat()

fun BigDecimal.roundToDefaultScale(): BigDecimal = setScale(2, RoundingMode.HALF_EVEN)

fun BigDecimal?.toCurrencyDisplayValue(scale: Int = 2) = "$${this.toDisplayValue(scale)}"

fun BigDecimal?.toDisplayValueAutoScale(): String =
    (this?.scale() ?: 2).provideDecimalWithScale().format(this ?: BigDecimal.ZERO)

fun BigDecimal?.toDisplayValue(scale: Int = 2): String =
    scale.provideDecimalWithScale().format(this ?: BigDecimal.ZERO)
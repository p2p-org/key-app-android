package org.p2p.core.utils

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import java.math.RoundingMode
import java.util.Locale

private const val DECIMAL_FORMAT = "###,###."

object DecimalFormatter {

    fun format(value: Number, decimals: Int): String {
        val format = DECIMAL_FORMAT + "#".repeat(decimals)

        val formatSymbols = DecimalFormatSymbols(Locale.ENGLISH).apply {
            decimalSeparator = '.'
            groupingSeparator = ' '
        }

        val decimalFormat = DecimalFormat(format, formatSymbols)
        decimalFormat.roundingMode = RoundingMode.DOWN.ordinal
        return decimalFormat.format(value)
    }
}

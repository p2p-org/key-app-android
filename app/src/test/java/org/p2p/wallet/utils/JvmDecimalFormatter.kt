package org.p2p.wallet.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private const val DECIMAL_FORMAT = "###,###."

object JvmDecimalFormatter {

    fun format(value: Number, decimals: Int): String {
        val format = DECIMAL_FORMAT + "#".repeat(decimals)

        val formatSymbols = DecimalFormatSymbols(Locale.ENGLISH).apply {
            decimalSeparator = '.'
            groupingSeparator = ' '
        }

        val decimalFormat = DecimalFormat(format, formatSymbols)
        decimalFormat.roundingMode = RoundingMode.DOWN
        return decimalFormat.format(value)
    }
}

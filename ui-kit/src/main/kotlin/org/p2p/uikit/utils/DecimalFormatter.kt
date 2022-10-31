package org.p2p.uikit.utils

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import java.util.Locale

private const val DECIMAL_FORMAT = "###,###."

object DecimalFormatter {

    fun format(value: Number, decimals: Int): String {
        val format = DECIMAL_FORMAT + "#".repeat(decimals)

        val formatSymbols = DecimalFormatSymbols(Locale.ENGLISH).apply {
            decimalSeparator = '.'
            groupingSeparator = ' '
        }

        return DecimalFormat(format, formatSymbols).format(value)
    }
}

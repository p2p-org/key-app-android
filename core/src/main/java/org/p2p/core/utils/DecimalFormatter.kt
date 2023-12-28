package org.p2p.core.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

private const val DECIMAL_FORMAT = "###,###."

object DecimalFormatter {

    /**
     * Format number with given decimals
     * Note: don't remove JvmStatic as it's used in tests
     */
    @JvmStatic
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

    @JvmStatic
    fun format(
        value: BigDecimal,
        decimals: Int,
        exactDecimals: Boolean = false,
        keepInitialDecimals: Boolean = false,
    ): String {
        val fmt = NumberFormat.getInstance(Locale.US) as DecimalFormat
        val symbols = fmt.decimalFormatSymbols
        symbols.groupingSeparator = ' '
        symbols.decimalSeparator = '.'
        if (exactDecimals) {
            fmt.minimumFractionDigits = decimals
            fmt.maximumFractionDigits = decimals
        } else {
            fmt.maximumFractionDigits = decimals
            fmt.minimumFractionDigits = if (keepInitialDecimals) clamp(value.scale(), 0, decimals) else 0
        }
        fmt.decimalFormatSymbols = symbols
        return fmt.format(value)
    }
}

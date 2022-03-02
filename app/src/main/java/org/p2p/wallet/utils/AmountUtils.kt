package org.p2p.wallet.utils

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object AmountUtils {

    private val formatter = NumberFormat.getInstance(Locale.US)

    /**
     * Adds thousands separator:
     * @sample 2834.32 -> 2,8324.32
     * @return string value
     *  */
    fun format(value: BigDecimal): String =
        formatter.format(value.toDouble())
}
package org.p2p.core.textwatcher

import org.p2p.core.utils.DecimalFormatter
import org.p2p.core.utils.emptyString

private const val SYMBOL_ZERO = "0"
private const val SYMBOL_DOT = "."
private const val SYMBOL_DOT_CHAR = '.'
private const val MAX_INT_LENGTH = 12

class AmountFractionFormatter(
    private val maxLengthAllowed: Int
) {
    fun formatAmountFraction(value: String, before: Int, start: Int): String {
        val hasMultipleDots = value.count { it == SYMBOL_DOT_CHAR } > 1
        return when {
            value.isEmpty() -> value
            // Remove whole string if dot before zero was removed
            value == SYMBOL_ZERO && before == 1 -> emptyString()
            value == "$SYMBOL_ZERO$SYMBOL_ZERO" && start == 1 -> SYMBOL_ZERO

            value.startsWith(SYMBOL_DOT) && hasMultipleDots -> value.drop(1)
            value.startsWith(SYMBOL_DOT) -> "$SYMBOL_ZERO$value"

            hasMultipleDots && value.endsWith(SYMBOL_DOT) -> handleEndsWithDotCaseWithMultipleDots(value)
            value.endsWith(SYMBOL_DOT) -> handleEndWithDotCase(value)

            value.contains(SYMBOL_DOT) -> handleValueWithDotCase(value)

            else -> handleGeneralCase(value)
        }
    }

    private fun handleEndsWithDotCaseWithMultipleDots(value: String): String {
        return value.dropLast(1)
            .dropSpaces()
            .formatDecimal(maxLengthAllowed)
    }

    private fun handleEndWithDotCase(value: String): String {
        return value.dropLast(1)
            .dropSpaces()
            .formatDecimal(maxLengthAllowed) + SYMBOL_DOT
    }

    private fun handleGeneralCase(value: String): String {
        return value.dropSpaces()
            .take(MAX_INT_LENGTH)
            .formatDecimal(maxLengthAllowed)
    }

    private fun handleValueWithDotCase(value: String): String {
        val dotPosition = value.indexOf(SYMBOL_DOT)
        val intPart = value.substring(0, dotPosition)
            .dropSpaces()
            .take(MAX_INT_LENGTH)

        // Remove extra dots and shorten fractional part to maxLengthAllowed symbols
        val fractionalPart = value.substring(dotPosition + 1)
            .dropSpaces()
            .replace(SYMBOL_DOT, emptyString())
            .take(maxLengthAllowed)

        return intPart.formatDecimal(maxLengthAllowed) + SYMBOL_DOT + fractionalPart
    }

    private fun String.dropSpaces(): String {
        return replace(" ", emptyString())
    }

    private fun String.formatDecimal(fractionLength: Int): String {
        return DecimalFormatter.format(this.toBigDecimal(), fractionLength)
    }
}

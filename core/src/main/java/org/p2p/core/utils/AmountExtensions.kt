package org.p2p.core.utils

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.pow
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.FIAT_FRACTION_LENGTH

private const val POWER_VALUE = 10.0
const val DEFAULT_DECIMAL = 9
const val MOONPAY_DECIMAL = 2

private const val SCALE_VALUE_SHORT = 2
private const val SCALE_VALUE_MEDIUM = 6
private const val SCALE_VALUE_LONG = 9

private const val AMOUNT_MIN_VALUE = 0.01

fun String?.toBigDecimalOrZero(): BigDecimal {
    val removedZeros = this?.replace("(?<=\\d)\\.?0+(?![\\d\\.])", emptyString())
    return removedZeros?.toBigDecimalOrNull() ?: BigDecimal.ZERO
}

fun Int.toPowerValue(): BigDecimal =
    BigDecimal(POWER_VALUE.pow(this))

fun BigDecimal.scaleShortOrFirstNotZero(): BigDecimal {
    return if (isZero()) {
        this
    } else {
        val scale = if (scale() > SCALE_VALUE_SHORT) {
            scale() - (unscaledValue().toString().length - SCALE_VALUE_SHORT)
        } else {
            SCALE_VALUE_SHORT
        }
        // removing zeros, case: 0.02000 -> 0.2
        setScale(scale, RoundingMode.HALF_EVEN).stripTrailingZeros()
    }
}

fun BigDecimal.scaleShort(): BigDecimal =
    this.setScale(SCALE_VALUE_SHORT, RoundingMode.HALF_EVEN)
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.02

fun BigDecimal.scaleMedium(): BigDecimal =
    if (this.isZero()) this else this.setScale(SCALE_VALUE_MEDIUM, RoundingMode.HALF_EVEN)
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.02

fun BigDecimal.scaleLong(decimals: Int = SCALE_VALUE_LONG): BigDecimal =
    if (this.isZero()) this else this.setScale(decimals, RoundingMode.HALF_EVEN)
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.02

fun BigInteger.fromLamports(decimals: Int = DEFAULT_DECIMAL): BigDecimal =
    BigDecimal(this.toDouble() / (POWER_VALUE.pow(decimals)))
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.02
        .scaleLong(decimals)

fun BigDecimal.toLamports(decimals: Int): BigInteger =
    this.multiply(decimals.toPowerValue()).toBigInteger()

fun BigDecimal.toUsd(usdRate: BigDecimal?): BigDecimal? =
    usdRate?.let { this.multiply(it).scaleShort() }

fun BigDecimal.toUsd(token: Token): BigDecimal? =
    token.rate?.let { this.multiply(it).scaleShort() }

// case: 1000.023000 -> 1 000.02
fun BigDecimal.formatFiat(): String = formatWithDecimals(FIAT_FRACTION_LENGTH)

// case: 10000.000000007900 -> 100 000.000000008
fun BigDecimal.formatToken(decimals: Int = DEFAULT_DECIMAL): String = formatWithDecimals(decimals)

// case: 10000.000000007900 -> 100 000.00
fun BigDecimal.formatTokenForMoonpay(): String = formatWithDecimals(MOONPAY_DECIMAL)

private fun BigDecimal.formatWithDecimals(decimals: Int): String = this.stripTrailingZeros().run {
    if (isZero()) this.toPlainString() else DecimalFormatter.format(this, decimals)
}

fun BigDecimal?.isNullOrZero(): Boolean = this == null || this.compareTo(BigDecimal.ZERO) == 0
fun BigDecimal.isZero() = this.compareTo(BigDecimal.ZERO) == 0
fun BigDecimal.isNotZero() = this.compareTo(BigDecimal.ZERO) != 0
fun BigDecimal.isMoreThan(value: BigDecimal) = this.compareTo(value) == 1
fun BigDecimal.isLessThan(value: BigDecimal) = this.compareTo(value) == -1
fun BigDecimal.isZeroOrLess() = isZero() || isLessThan(BigDecimal.ZERO)

fun BigDecimal?.orZero(): BigDecimal = this ?: BigDecimal.ZERO
fun BigInteger?.orZero(): BigInteger = this ?: BigInteger.ZERO

fun BigInteger.isZero() = this.compareTo(BigInteger.ZERO) == 0
fun BigInteger.isNotZero() = this.compareTo(BigInteger.ZERO) != 0
fun BigInteger.isLessThan(value: BigInteger) = this.compareTo(value) == -1
fun BigInteger.isMoreThan(value: BigInteger) = this.compareTo(value) == 1
fun BigInteger.isZeroOrLess() = isZero() || isLessThan(BigInteger.ZERO)

fun BigDecimal.asCurrency(currency: String): String =
    if (lessThenMinValue()) "<$currency 0.01" else "$currency ${formatFiat()}"

fun BigDecimal.asUsd(): String = if (lessThenMinValue()) "<$ 0.01" else "$ ${formatFiat()}"
fun BigDecimal.asApproximateUsd(withBraces: Boolean = true): String = when {
    lessThenMinValue() -> "(<$0.01)"
    withBraces -> "~($${formatFiat()})"
    else -> "~$${formatFiat()}"
}

fun BigDecimal.asPositiveUsdTransaction(): String = asUsdTransaction("+")
fun BigDecimal.asNegativeUsdTransaction(): String = asUsdTransaction("-")
fun BigDecimal.asUsdTransaction(
    transactionSymbol: String
): String = if (lessThenMinValue()) "<$ 0.01" else "$transactionSymbol$ ${formatFiat()}"

fun BigDecimal.asUsdSwap(): String = when {
    isZero() -> "0 USD"
    lessThenMinValue() -> "<0.01 USD"
    else -> "≈${formatFiat()} USD"
}

fun Int?.orZero(): Int = this ?: 0

// value is in (0..0.01)
fun BigDecimal.lessThenMinValue() = !isZero() && isLessThan(AMOUNT_MIN_VALUE.toBigDecimal())

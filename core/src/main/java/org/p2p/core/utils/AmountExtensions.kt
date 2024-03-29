package org.p2p.core.utils

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.FIAT_FRACTION_LENGTH

private val BASE_TEN = BigDecimal.TEN
const val SOL_DECIMALS = 9
const val MOONPAY_DECIMAL = 2
const val STRIGA_FIAT_DECIMALS = 2

private const val SCALE_VALUE_TWO = 2
private const val SCALE_VALUE_SIX = 6
private const val SCALE_VALUE_NINE = 9

private const val AMOUNT_MIN_VALUE = 0.01

fun String?.toBigDecimalOrZero(): BigDecimal {
    val removedZeros = this?.replace("(?<=\\d)\\.?0+(?![\\d\\.])", emptyString())
    return removedZeros?.toBigDecimalOrNull().orZero()
}

fun String?.toBigIntegerOrZero(): BigInteger {
    return this?.toBigIntegerOrNull().orZero()
}

fun Int.toPowerValue(): BigDecimal = BASE_TEN.pow(this)

fun BigDecimal.scaleTwoOrFirstNotZero(): BigDecimal {
    if (isZero()) {
        return this
    }
    val scale = if (scale() > SCALE_VALUE_TWO) {
        scale() - (unscaledValue().toString().length - SCALE_VALUE_TWO)
    } else {
        SCALE_VALUE_TWO
    }
    // removing zeros, case: 0.02000 -> 0.2
    return setScale(scale, RoundingMode.DOWN).stripTrailingZeros()
}

fun BigDecimal.scaleTwo(): BigDecimal =
    this.setScale(SCALE_VALUE_TWO, RoundingMode.DOWN)
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.02

fun BigDecimal.scaleSix(): BigDecimal =
    if (this.isZero()) this else this.setScale(SCALE_VALUE_SIX, RoundingMode.DOWN)
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.02

fun BigDecimal.scaleNine(decimals: Int = SCALE_VALUE_NINE): BigDecimal =
    if (this.isZero()) this else this.setScale(decimals, RoundingMode.DOWN)
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.02

// do not use BigDecimal(double) sometimes it makes the amount less
// example: pass 0.030, get 0.029
fun BigInteger.fromLamports(decimals: Int): BigDecimal =
    (this.toBigDecimal().divide(BASE_TEN.pow(decimals), 18, RoundingMode.HALF_DOWN))
        .stripTrailingZeros() // removing zeros, case: 0.02000 -> 0.02
        .scaleNine(decimals)

fun BigDecimal.toLamports(decimals: Int): BigInteger =
    this.multiply(decimals.toPowerValue()).toBigInteger()

fun BigDecimal.toUsd(usdRate: BigDecimal?): BigDecimal? =
    usdRate?.let { this.multiply(it).scaleTwo() }

fun BigDecimal.toUsd(token: Token): BigDecimal? =
    token.rate?.let { this.multiply(it).scaleTwo() }

// case: 1000.023000 -> 1 000.02
fun BigDecimal.formatFiat(): String = formatWithDecimals(FIAT_FRACTION_LENGTH)

// case 1: 10000.000000007900 -> 100 000.000000008
// case 2: 1.0 -> 1 - default behavior
// case 3: 1.0 -> 1.0 -> with keepInitialDecimals=true
fun BigDecimal.formatToken(
    decimals: Int = SOL_DECIMALS,
    exactDecimals: Boolean = false,
    keepInitialDecimals: Boolean = false,
): String = formatWithDecimals(
    decimals = decimals,
    exactDecimals = exactDecimals,
    keepInitialDecimals = keepInitialDecimals
)

fun BigDecimal.formatTokenWithSymbol(
    tokenSymbol: String,
    decimals: Int,
    exactDecimals: Boolean = false,
    keepInitialDecimals: Boolean = false,
): String {
    val formattedAmount = formatWithDecimals(
        decimals = decimals,
        exactDecimals = exactDecimals,
        keepInitialDecimals = keepInitialDecimals
    )
    return "$formattedAmount $tokenSymbol"
}

fun BigDecimal.formatTokenWithSymbol(
    token: Token.Active,
    exactDecimals: Boolean = false,
    keepInitialDecimals: Boolean = false,
): String = formatTokenWithSymbol(
    token.tokenSymbol,
    token.decimals,
    exactDecimals,
    keepInitialDecimals
)

// case: 10000.000000007900 -> 100 000.00
fun BigDecimal.formatTokenForMoonpay(): String = formatWithDecimals(MOONPAY_DECIMAL)

/**
 * Note: setScale(0) for zero is mandatory because if the value has precision greater than 6 decimals,
 * the result of toString() will be formatted using scientific notation
 * @param decimals - number of decimals to show
 * @param exactDecimals - format with exact number of decimals
 * @param keepInitialDecimals - keep initial decimals
 * @return formatted string
 */
fun BigDecimal.formatWithDecimals(
    decimals: Int,
    exactDecimals: Boolean = false,
    keepInitialDecimals: Boolean = false,
): String {
    val source = if (keepInitialDecimals) this else stripTrailingZeros()

    return with(source) {
        if (isZero()) {
            this.setScale(0).toString()
        } else {
            DecimalFormatter.format(
                value = this,
                decimals = decimals,
                exactDecimals = exactDecimals,
                keepInitialDecimals = keepInitialDecimals,
            )
        }
    }
}

fun BigDecimal?.isNullOrZero(): Boolean = this == null || this.compareTo(BigDecimal.ZERO) == 0
fun BigDecimal.isZero(): Boolean = this.compareTo(BigDecimal.ZERO) == 0
fun BigDecimal.isNotZero(): Boolean = this.compareTo(BigDecimal.ZERO) != 0
fun BigDecimal.isMoreThan(value: BigDecimal): Boolean = this.compareTo(value) == 1
fun BigDecimal.isLessThan(value: BigDecimal): Boolean = this.compareTo(value) == -1
fun BigDecimal.isZeroOrLess(): Boolean = isZero() || isLessThan(BigDecimal.ZERO)

fun BigDecimal?.orZero(): BigDecimal = this ?: BigDecimal.ZERO
fun BigInteger?.orZero(): BigInteger = this ?: BigInteger.ZERO

fun BigInteger.isZero(): Boolean = this.compareTo(BigInteger.ZERO) == 0
fun BigInteger.isNotZero(): Boolean = this.compareTo(BigInteger.ZERO) != 0
fun BigInteger.isLessThan(value: BigInteger) = this.compareTo(value) == -1
fun BigInteger.isMoreThan(value: BigInteger) = this.compareTo(value) == 1
fun BigInteger.isZeroOrLess(): Boolean = isZero() || isLessThan(BigInteger.ZERO)

fun BigDecimal.asCurrency(currencyUiSymbol: String): String = when {
    isZero() -> "${currencyUiSymbol}0"
    lessThenMinValue() -> "<${currencyUiSymbol}0.01"
    else -> "${currencyUiSymbol}${formatFiat()}"
}

// TODO refactor after we will migrate on currency as an entity
fun BigDecimal.asCurrencyAfter(currency: String): String = when {
    isZero() -> "0 $currency"
    lessThenMinValue() -> "<0.01 $currency"
    else -> "${formatFiat()} $currency"
}

fun BigDecimal.asUsd(): String = asCurrency(Constants.USD_SYMBOL)

fun BigDecimal.asApproximateUsd(withBraces: Boolean = true): String = when {
    isZero() -> "$0"
    lessThenMinValue() -> "(<$0.01)"
    withBraces -> "(~$${formatFiat()})"
    else -> "~$${formatFiat()}"
}

fun BigDecimal.asPositiveUsdTransaction(): String = asUsdTransaction("+")
fun BigDecimal.asNegativeUsdTransaction(): String = asUsdTransaction("-")
fun BigDecimal.asUsdTransaction(
    transactionSymbol: String
): String {
    if (lessThenMinValue()) return "<$0.01"
    val fiatAmountWithSymbol = "$${formatFiat()}"
    return "$transactionSymbol $fiatAmountWithSymbol"
}

fun BigDecimal.asUsdSwap(): String = when {
    isZero() -> "0 USD"
    lessThenMinValue() -> "<0.01 USD"
    else -> "≈${formatFiat()} USD"
}

fun Int?.orZero(): Int = this ?: 0

// value is in (0..0.01)
fun BigDecimal.lessThenMinValue(): Boolean = !isZero() && isLessThan(AMOUNT_MIN_VALUE.toBigDecimal())
fun BigDecimal.moreThenMinValue(): Boolean = isMoreThan(AMOUNT_MIN_VALUE.toBigDecimal())
fun BigDecimal.divideByInt(byInt: Int): BigDecimal = divide(byInt.toBigDecimal())

package org.p2p.wallet.newsend.model

import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.formatUsd
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.send.model.CurrencyMode
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

private const val ROUNDING_VALUE = 6

class CalculationMode {

    var onCalculationCompleted: ((aroundValue: String) -> Unit)? = null
    var onLabelsUpdated: ((switchSymbol: String, mainSymbol: String) -> Unit)? = null

    private var currencyMode: CurrencyMode = CurrencyMode.Usd

    private lateinit var token: Token.Active

    private var inputAmount: String = emptyString()

    private var tokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

    fun updateToken(newToken: Token.Active) {
        this.token = newToken

        if (currencyMode is CurrencyMode.Token) {
            currencyMode = CurrencyMode.Token(newToken.tokenSymbol)
        }

        updateLabels()
    }

    fun updateInputAmount(newInputAmount: String) {
        this.inputAmount = newInputAmount
        recalculate(inputAmount)
    }

    fun reduceAmount(newInputAmount: BigInteger): BigDecimal {
        val newAmount = newInputAmount.fromLamports(token.decimals)
        return if (currencyMode is CurrencyMode.Usd) {
            newAmount.toUsd(token).orZero()
        } else {
            newAmount
        }
    }

    fun getCurrentAmountLamports(): BigInteger = tokenAmount.toLamports(token.decimals)

    fun getCurrentAmount(): BigDecimal = tokenAmount

    fun getCurrentAmountUsd(): BigDecimal = usdAmount

    fun getTotalAvailable(): BigDecimal? {
        return when (currencyMode) {
            is CurrencyMode.Usd -> token.totalInUsd
            is CurrencyMode.Token -> token.total.scaleLong()
        }
    }

    fun switchMode() {
        currencyMode = when (currencyMode) {
            is CurrencyMode.Token -> CurrencyMode.Usd
            is CurrencyMode.Usd -> CurrencyMode.Token(token.tokenSymbol)
        }
        updateLabels()
    }

    private fun updateLabels() {
        val (switchSymbol, mainSymbol) = when (currencyMode) {
            is CurrencyMode.Token -> USD_READABLE_SYMBOL to token.tokenSymbol
            is CurrencyMode.Usd -> token.tokenSymbol to USD_READABLE_SYMBOL
        }

        onLabelsUpdated?.invoke(switchSymbol, mainSymbol)

        recalculate(inputAmount)
    }

    private fun recalculate(inputAmount: String) {
        when (currencyMode) {
            is CurrencyMode.Token -> calculateByToken(inputAmount)
            is CurrencyMode.Usd -> calculateByUsd(inputAmount)
        }
    }

    private fun calculateByUsd(inputAmount: String) {
        usdAmount = inputAmount.toBigDecimalOrZero()
        tokenAmount = if (token.usdRateOrZero.isZero()) {
            BigDecimal.ZERO
        } else {
            usdAmount.divide(token.usdRateOrZero, ROUNDING_VALUE, RoundingMode.HALF_EVEN).stripTrailingZeros()
        }

        val tokenAround = if (usdAmount.isZero() || token.usdRateOrZero.isZero()) {
            BigDecimal.ZERO
        } else {
            usdAmount.divide(token.usdRateOrZero, ROUNDING_VALUE, RoundingMode.HALF_EVEN).stripTrailingZeros()
        }

        onCalculationCompleted?.invoke("${tokenAround.formatToken()} ${token.tokenSymbol}")
    }

    private fun calculateByToken(inputAmount: String) {
        tokenAmount = inputAmount.toBigDecimalOrZero()
        usdAmount = tokenAmount.multiply(token.usdRateOrZero)

        val usdAround = tokenAmount.times(token.usdRateOrZero)
        onCalculationCompleted?.invoke("${usdAround.formatUsd()} $USD_READABLE_SYMBOL")
    }
}

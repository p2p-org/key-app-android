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
import org.p2p.wallet.utils.divideSafe
import java.math.BigDecimal
import java.math.BigInteger

private const val FIAT_FRACTION_LENGTH = 2

class CalculationMode {

    var onCalculationCompleted: ((aroundValue: String) -> Unit)? = null
    var onInputFractionUpdated: ((Int) -> Unit)? = null
    var onLabelsUpdated: ((switchSymbol: String, mainSymbol: String) -> Unit)? = null

    private var currencyMode: CurrencyMode = CurrencyMode.Usd

    private lateinit var token: Token.Active

    private var inputAmount: String = emptyString()

    private var tokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

    fun updateToken(newToken: Token.Active) {
        this.token = newToken

        if (currencyMode is CurrencyMode.Token) {
            currencyMode = CurrencyMode.Token(newToken)
        }

        handleFractionUpdate(currencyMode)

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

    fun getValueByMode(): BigDecimal = if (currencyMode is CurrencyMode.Token) {
        tokenAmount
    } else {
        usdAmount
    }

    fun getSymbolByMode(): String = if (currencyMode is CurrencyMode.Token) {
        token.tokenSymbol
    } else {
        USD_READABLE_SYMBOL
    }

    fun getCurrentMode(): CurrencyMode = currencyMode

    fun getTotalAvailable(): BigDecimal? {
        return when (currencyMode) {
            is CurrencyMode.Usd -> token.totalInUsd
            is CurrencyMode.Token -> token.total.scaleLong()
        }
    }

    fun switchMode() {
        currencyMode = when (currencyMode) {
            is CurrencyMode.Token -> CurrencyMode.Usd
            is CurrencyMode.Usd -> CurrencyMode.Token(token)
        }

        handleFractionUpdate(currencyMode)
        updateLabels()
    }

    fun isMaxButtonVisible(minRentExemption: BigInteger): Boolean {
        return if (token.isSOL) {
            val maxAllowedAmount = token.totalInLamports - minRentExemption
            val amountInLamports = tokenAmount.toLamports(token.decimals)
            inputAmount.isEmpty() || amountInLamports > maxAllowedAmount && amountInLamports < token.totalInLamports
        } else {
            inputAmount.isEmpty()
        }
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

        val tokenAround = if (usdAmount.isZero() || token.usdRateOrZero.isZero()) {
            BigDecimal.ZERO
        } else {
            usdAmount.divideSafe(token.usdRateOrZero).scaleLong()
        }

        tokenAmount = tokenAround

        onCalculationCompleted?.invoke("${tokenAround.formatToken()} ${token.tokenSymbol}")
    }

    private fun calculateByToken(inputAmount: String) {
        tokenAmount = inputAmount.toBigDecimalOrZero()
        usdAmount = tokenAmount.multiply(token.usdRateOrZero)

        val usdAround = tokenAmount.times(token.usdRateOrZero)
        onCalculationCompleted?.invoke("${usdAround.formatUsd()} $USD_READABLE_SYMBOL")
    }

    private fun handleFractionUpdate(mode: CurrencyMode) {
        val newInputFractionLength = when (mode) {
            is CurrencyMode.Token -> mode.fractionLength
            is CurrencyMode.Usd -> FIAT_FRACTION_LENGTH
        }

        onInputFractionUpdated?.invoke(newInputFractionLength)
    }
}

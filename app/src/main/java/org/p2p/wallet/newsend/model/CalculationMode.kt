package org.p2p.wallet.newsend.model

import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.formatUsd
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.lessThenMinValue
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.send.model.CurrencyMode
import org.p2p.wallet.utils.divideSafe
import java.math.BigDecimal
import java.math.BigInteger

private const val FIAT_FRACTION_LENGTH = 2

class CalculationMode(
    private val sendModeProvider: SendModeProvider,
    private val lessThenMinString: String
) {

    var onCalculationCompleted: ((aroundValue: String) -> Unit)? = null
    var onInputFractionUpdated: ((Int) -> Unit)? = null
    var onLabelsUpdated: ((switchSymbol: String, mainSymbol: String) -> Unit)? = null

    private var currencyMode: CurrencyMode = sendModeProvider.sendMode
        set(value) {
            sendModeProvider.sendMode = value
            field = value
        }

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

    fun reduceAmount(newInputAmountInToken: BigInteger): BigDecimal {
        val newTokenAmount = newInputAmountInToken.fromLamports(token.decimals)
        val newUsdAmount = newTokenAmount.toUsd(token).orZero()
        val newAmount = if (currencyMode is CurrencyMode.Usd) newUsdAmount else newTokenAmount

        usdAmount = newUsdAmount
        tokenAmount = newTokenAmount
        inputAmount = newAmount.toString()

        if (currencyMode is CurrencyMode.Usd) {
            handleCalculateTokenAmountUpdate()
        } else {
            handleCalculateUsdAmountUpdate()
        }

        return newAmount
    }

    fun getCurrentAmountLamports(): BigInteger = tokenAmount.toLamports(token.decimals)

    fun getCurrentAmount(): BigDecimal = tokenAmount

    fun getCurrentAmountUsd(): BigDecimal = usdAmount

    fun isCurrentInputEmpty(): Boolean = inputAmount.isEmpty()

    fun getMaxAvailableAmount(): BigDecimal? {
        tokenAmount = token.total
        usdAmount = token.totalInUsd.orZero()

        val maxAmount = when (currencyMode) {
            is CurrencyMode.Usd -> {
                handleCalculateTokenAmountUpdate()
                token.totalInUsd
            }
            is CurrencyMode.Token -> {
                handleCalculateUsdAmountUpdate()
                token.total.scaleLong()
            }
        }

        inputAmount = maxAmount?.toString().orEmpty()

        return maxAmount
    }

    fun switchMode(): CurrencyMode {
        currencyMode = when (currencyMode) {
            is CurrencyMode.Token -> CurrencyMode.Usd
            is CurrencyMode.Usd -> CurrencyMode.Token(token)
        }

        handleFractionUpdate(currencyMode)
        updateLabels()

        return currencyMode
    }

    fun getCurrencyMode(): CurrencyMode = currencyMode

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

        val tokenAround = usdAmount.divideSafe(token.usdRateOrZero, token.decimals)
        tokenAmount = tokenAround

        handleCalculateTokenAmountUpdate()
    }

    private fun calculateByToken(inputAmount: String) {
        tokenAmount = inputAmount.toBigDecimalOrZero()
        usdAmount = tokenAmount.multiply(token.usdRateOrZero)

        handleCalculateUsdAmountUpdate()
    }

    private fun handleCalculateTokenAmountUpdate() {
        handleCalculationUpdate(tokenAmount.formatToken(token.decimals), token.tokenSymbol)
    }

    private fun handleCalculateUsdAmountUpdate() {
        val formattedUsdAmount = if (usdAmount.lessThenMinValue()) lessThenMinString else usdAmount.formatUsd()
        handleCalculationUpdate(formattedUsdAmount, USD_READABLE_SYMBOL)
    }

    private fun handleFractionUpdate(mode: CurrencyMode) {
        val newInputFractionLength = when (mode) {
            is CurrencyMode.Token -> mode.fractionLength
            is CurrencyMode.Usd -> FIAT_FRACTION_LENGTH
        }

        onInputFractionUpdated?.invoke(newInputFractionLength)
    }

    private fun handleCalculationUpdate(value: String, symbol: String) {
        onCalculationCompleted?.invoke("$value $symbol")
    }
}

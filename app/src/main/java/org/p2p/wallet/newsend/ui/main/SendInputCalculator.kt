package org.p2p.wallet.newsend.ui.main

import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isZero
import org.p2p.core.utils.lessThenMinValue
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.newsend.model.CalculationState
import org.p2p.wallet.utils.divideSafe

class SendInputCalculator(
    private val sendModeProvider: SendModeProvider,
    private val lessThenMinString: String
) {

    private val calculationState = MutableStateFlow<CalculationState>(CalculationState.Idle)

    private var currencyMode: CurrencyMode = sendModeProvider.sendMode
        set(value) {
            sendModeProvider.sendMode = value
            field = value
        }

    private lateinit var currentToken: Token.Active

    private var inputAmount: String = emptyString()
    private var tokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

    private var minRentExemption: BigInteger = BigInteger.ZERO

    fun getStateFlow(): StateFlow<CalculationState> = calculationState.asStateFlow()

    fun updateToken(newToken: Token.Active) {
        if (!::currentToken.isInitialized || newToken.mintAddress != currentToken.mintAddress) {
            this.currentToken = newToken.also { updateTokenMode(it) }
            emitFractionUpdate(currencyMode)
        }

        updateLabels()
    }

    fun updateInputAmount(newInputAmount: String) {
        this.inputAmount = newInputAmount
        recalculate(inputAmount)

        val isMaxButtonVisible = isMaxButtonVisible(minRentExemption)
        val newState = CalculationState.MaxButtonVisible(isMaxButtonVisible = isMaxButtonVisible)
        updateState(newState)
    }

    fun saveMinRentExemption(minRentExemption: BigInteger) {
        this.minRentExemption = minRentExemption
    }

    fun reduceAmount(newTokenAmount: BigDecimal): BigDecimal {
        val newUsdAmount = newTokenAmount.toUsd(currentToken).orZero()
        val newAmount = if (currencyMode is CurrencyMode.Fiat) newUsdAmount else newTokenAmount

        usdAmount = newUsdAmount
        tokenAmount = newTokenAmount
        inputAmount = newAmount.toString()

        if (currencyMode is CurrencyMode.Fiat) {
            formatTokenAmount()
        } else {
            formatUsdAmount()
        }

        return newAmount
    }

    fun getCurrentAmount(): BigDecimal = tokenAmount

    fun getCurrentAmountUsd(): BigDecimal? = usdAmount.takeIf { currentToken.rate != null }

    fun onMaxClicked() {
        tokenAmount = currentToken.total
        usdAmount = currentToken.totalInUsdScaled.orZero()

        val maxAmount = when (currencyMode) {
            is CurrencyMode.Fiat -> {
                formatTokenAmount()
                currentToken.totalInUsdScaled
            }
            is CurrencyMode.Token -> {
                formatUsdAmount()
                currentToken.total.scaleLong()
            }
        }

        inputAmount = maxAmount?.toString().orEmpty()

        val isMaxButtonVisible = isMaxButtonVisible(minRentExemption)
        val newCalculationState = CalculationState.MaxValueEntered(
            newInputAmount = inputAmount,
            isMaxButtonVisible = isMaxButtonVisible,
            sourceTokenSymbol = currentToken.tokenSymbol
        )
        updateState(newCalculationState)
    }

    fun toggleMode() {
        currencyMode = when (currencyMode) {
            is CurrencyMode.Token -> CurrencyMode.Fiat.Usd // only support USD
            is CurrencyMode.Fiat -> CurrencyMode.Token(currentToken)
        }

        emitFractionUpdate(currencyMode)

        val (switchSymbol, mainSymbol) = if (currencyMode.isFiat()) {
            inputAmount = usdAmount.toPlainString()
            currencyMode.getSymbol() to currentToken.tokenSymbol
        } else {
            inputAmount = tokenAmount.toPlainString()
            currentToken.tokenSymbol to currencyMode.getSymbol()
        }

        val newState = CalculationState.CurrencySwitched(
            newInputAmount = inputAmount,
            switchSymbol = switchSymbol,
            mainSymbol = mainSymbol,
            isFiat = currencyMode.isFiat()
        )
        updateState(newState)
    }

    fun enableTokenMode() {
        if (currencyMode is CurrencyMode.Fiat.Usd) {
            toggleMode()
        }
    }

    private fun isMaxButtonVisible(minRentExemption: BigInteger): Boolean {
        return if (currentToken.isSOL) {
            val maxAllowedAmount = currentToken.totalInLamports - minRentExemption
            val amountInLamports = tokenAmount.toLamports(currentToken.decimals)
            inputAmount.isEmpty() || amountInLamports >= maxAllowedAmount && amountInLamports < currentToken.totalInLamports
        } else {
            inputAmount.isEmpty()
        }
    }

    private fun updateTokenMode(newToken: Token.Active) {
        if (currencyMode is CurrencyMode.Token) {
            currencyMode = CurrencyMode.Token(newToken)
        }
    }

    private fun updateLabels() {
        val (switchSymbol, mainSymbol) = when (val mode = currencyMode) {
            is CurrencyMode.Token -> USD_READABLE_SYMBOL to currentToken.tokenSymbol
            is CurrencyMode.Fiat -> currentToken.tokenSymbol to mode.fiatAbbreviation
        }

        updateState(CalculationState.LabelsUpdate(switchSymbol = switchSymbol, mainSymbol = mainSymbol))

        recalculate(inputAmount)
    }

    private fun recalculate(inputAmount: String) {
        when (currencyMode) {
            is CurrencyMode.Token -> calculateByToken(inputAmount)
            is CurrencyMode.Fiat -> calculateByUsd(inputAmount)
        }
    }

    private fun calculateByUsd(inputAmount: String) {
        usdAmount = inputAmount.toBigDecimalOrZero()

        val tokenAround = usdAmount.divideSafe(currentToken.usdRateOrZero, currentToken.decimals)
        tokenAmount = tokenAround

        formatTokenAmount()
    }

    private fun calculateByToken(inputAmount: String) {
        tokenAmount = inputAmount.toBigDecimalOrZero()
        usdAmount = if (tokenAmount.isZero()) BigDecimal.ZERO else tokenAmount.multiply(currentToken.usdRateOrZero)

        formatUsdAmount()
    }

    private fun formatTokenAmount() {
        handleCalculationUpdate(tokenAmount.formatToken(currentToken.decimals), currentToken.tokenSymbol)
    }

    private fun formatUsdAmount() {
        val formattedUsdAmount = if (usdAmount.lessThenMinValue()) lessThenMinString else usdAmount.formatFiat()
        handleCalculationUpdate(formattedUsdAmount, USD_READABLE_SYMBOL)
    }

    private fun emitFractionUpdate(mode: CurrencyMode) {
        val newState = CalculationState.InputFractionUpdate(fraction = mode.fractionLength)
        updateState(newState)
    }

    private fun handleCalculationUpdate(value: String, symbol: String) {
        val state = CalculationState.CalculationCompleted("$value $symbol")
        updateState(state)
    }

    private fun updateState(newState: CalculationState) {
        calculationState.value = newState
    }
}

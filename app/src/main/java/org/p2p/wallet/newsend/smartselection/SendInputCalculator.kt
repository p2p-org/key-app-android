package org.p2p.wallet.newsend.smartselection

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
import org.p2p.wallet.newsend.model.CalculationEvent
import org.p2p.wallet.utils.divideSafe

/**
 * This class is responsible for user interaction with Send widget. [UiKitSendDetailsWidget]
 * 1. It calculates approximate amount in USD and token based on user input or vice-versa
 * 2. It handles currency mode and token switching
 * 3. Other similar stuff
 * */
class SendInputCalculator(
    private val sendModeProvider: SendModeProvider,
    private val lessThenMinString: String
) {
    private val calculationEvent = MutableStateFlow<CalculationEvent>(CalculationEvent.Idle)

    var currencyMode: CurrencyMode = sendModeProvider.sendMode
        set(value) {
            sendModeProvider.sendMode = value
            field = value
        }

    private lateinit var currentToken: Token.Active

    private var inputAmount: String = emptyString()
    private var approximateAmount: String = emptyString()

    private var tokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

    private var minRentExemption: BigInteger = BigInteger.ZERO

    fun getCalculationEventFlow(): StateFlow<CalculationEvent> = calculationEvent.asStateFlow()

    fun updateToken(newToken: Token.Active) {
        if (::currentToken.isInitialized && newToken.mintAddress == currentToken.mintAddress) {
            return
        }

        currentToken = newToken

        if (currencyMode is CurrencyMode.Token) {
            currencyMode = CurrencyMode.Token(newToken)
        }

        val (currentInputSymbol, switchInputSymbol) = currencyMode.getInputSymbols(currentToken)

        calculateApproximateAmount(inputAmount)

        val newEvent = CalculationEvent.TokenUpdated(
            currentInputSymbol = currentInputSymbol,
            switchInputSymbol = switchInputSymbol,
            fraction = currencyMode.fractionLength,
            approximateAmount = approximateAmount
        )
        updateEvent(newEvent)
    }

    fun updateInputAmount(newInputAmount: String) {
        inputAmount = newInputAmount

        calculateApproximateAmount(inputAmount)

        val newEvent = CalculationEvent.AmountChanged(
            approximateAmount = approximateAmount,
            isMaxButtonVisible = isMaxButtonVisible(minRentExemption)
        )
        updateEvent(newEvent)
    }

    fun saveMinRentExemption(minRentExemption: BigInteger) {
        this.minRentExemption = minRentExemption
    }

    fun reduceAmount(newTokenAmount: BigDecimal) {
        val newUsdAmount = newTokenAmount.toUsd(currentToken).orZero()
        val newAmount = if (currencyMode is CurrencyMode.Fiat) newUsdAmount else newTokenAmount
        usdAmount = newUsdAmount
        tokenAmount = newTokenAmount
        inputAmount = newAmount.toString()

        if (currencyMode is CurrencyMode.Fiat) {
            calculateApproximateTokenAmount()
        } else {
            calculateApproximateUsdAmount()
        }

        val newEvent = CalculationEvent.AmountReduced(
            approximateAmount = approximateAmount,
            isMaxButtonVisible = isMaxButtonVisible(minRentExemption),
            newInputAmount = inputAmount
        )
        updateEvent(newEvent)
    }

    fun getCurrentAmount(): BigDecimal = tokenAmount

    fun getCurrentAmountLamports(): BigInteger = tokenAmount.toLamports(currentToken.decimals)

    fun getCurrentAmountUsd(): BigDecimal? = usdAmount.takeIf { currentToken.rate != null }

    fun onMaxClicked() {
        tokenAmount = currentToken.total
        usdAmount = currentToken.totalInUsdScaled.orZero()

        val newInputAmount = when (currencyMode) {
            is CurrencyMode.Fiat -> {
                calculateApproximateTokenAmount()
                currentToken.totalInUsdScaled?.toString().orEmpty()
            }
            is CurrencyMode.Token -> {
                calculateApproximateUsdAmount()
                currentToken.total.scaleLong().toString()
            }
        }

        inputAmount = newInputAmount

        val isMaxButtonVisible = isMaxButtonVisible(minRentExemption)
        val newCalculationEvent = CalculationEvent.MaxValueEntered(
            approximateAmount = approximateAmount,
            newInputAmount = inputAmount,
            isMaxButtonVisible = isMaxButtonVisible,
            sourceTokenSymbol = currentToken.tokenSymbol
        )
        updateEvent(newCalculationEvent)
    }

    fun toggleMode() {
        val oldMode = currencyMode
        val newMode = currencyMode.toggle(currentToken).also { currencyMode = it }

        when (newMode) {
            is CurrencyMode.Token -> {
                val fiat = oldMode as CurrencyMode.Fiat

                approximateAmount = "$usdAmount ${fiat.fiatAbbreviation}"
                inputAmount = tokenAmount.toPlainString()
            }
            is CurrencyMode.Fiat -> {
                val token = oldMode as CurrencyMode.Token

                approximateAmount = "${tokenAmount.toPlainString()} ${token.symbol}"
                inputAmount = usdAmount.toPlainString()
            }
        }

        val (currentInput, switchInput) = newMode.getInputSymbols(currentToken)

        val newEvent = CalculationEvent.CurrencySwitched(
            newInputAmount = inputAmount,
            approximateAmount = approximateAmount,
            currentInputSymbol = currentInput,
            switchInputSymbol = switchInput,
            fraction = currencyMode.fractionLength
        )
        updateEvent(newEvent)
    }

    fun enableTokenMode() {
        if (currencyMode is CurrencyMode.Fiat.Usd) {
            toggleMode()
        }
    }

    private fun isMaxButtonVisible(minRentExemption: BigInteger): Boolean = if (currentToken.isSOL) {
        val maxAllowedAmount = currentToken.totalInLamports - minRentExemption
        val amountInLamports = tokenAmount.toLamports(currentToken.decimals)
        inputAmount.isEmpty() || amountInLamports >= maxAllowedAmount && amountInLamports < currentToken.totalInLamports
    } else {
        inputAmount.isEmpty()
    }

    private fun calculateApproximateAmount(inputAmount: String) {
        when (currencyMode) {
            is CurrencyMode.Token -> calculateByToken(inputAmount)
            is CurrencyMode.Fiat -> calculateByUsd(inputAmount)
        }
    }

    private fun calculateByUsd(inputAmount: String) {
        usdAmount = inputAmount.toBigDecimalOrZero()
        val tokenAround = usdAmount.divideSafe(currentToken.usdRateOrZero, currentToken.decimals)
        tokenAmount = tokenAround
        calculateApproximateTokenAmount()
    }

    private fun calculateByToken(inputAmount: String) {
        tokenAmount = inputAmount.toBigDecimalOrZero()
        usdAmount = if (tokenAmount.isZero()) BigDecimal.ZERO else tokenAmount.multiply(currentToken.usdRateOrZero)
        calculateApproximateUsdAmount()
    }

    private fun calculateApproximateTokenAmount() {
        approximateAmount = "${tokenAmount.formatToken(currentToken.decimals)} ${currentToken.tokenSymbol}"
    }

    private fun calculateApproximateUsdAmount() {
        val formattedUsdAmount = if (usdAmount.lessThenMinValue()) lessThenMinString else usdAmount.formatFiat()
        approximateAmount = "$formattedUsdAmount $USD_READABLE_SYMBOL"
    }

    private fun updateEvent(newEvent: CalculationEvent) {
        calculationEvent.value = newEvent
    }
}

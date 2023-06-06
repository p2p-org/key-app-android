package org.p2p.wallet.newsend.model

import org.p2p.wallet.newsend.model.CalculationState.CalculationCompleted
import org.p2p.wallet.newsend.model.CalculationState.InputFractionUpdate
import org.p2p.wallet.newsend.model.CalculationState.LabelsUpdate
import org.p2p.wallet.newsend.model.CalculationState.Idle
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isZero
import org.p2p.core.utils.lessThenMinValue
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.utils.divideSafe

class CalculationMode(
    private val sendModeProvider: SendModeProvider,
    private val lessThenMinString: String
) {

    private val calculationState: MutableStateFlow<CalculationState> = MutableStateFlow(Idle)
    var currencyMode: CurrencyMode = sendModeProvider.sendMode
        set(value) {
            sendModeProvider.sendMode = value
            field = value
        }

    private lateinit var token: Token.Active

    private var inputAmount: String = emptyString()

    var inputAmountDecimal: BigDecimal = BigDecimal.ZERO
        private set

    val formatInputAmount: String
        get() = when (currencyMode) {
            is CurrencyMode.Fiat -> inputAmountDecimal.formatFiat()
            is CurrencyMode.Token -> inputAmountDecimal.formatToken(token.decimals)
        }

    private var tokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

    fun getCalculationStateFlow(): Flow<CalculationState> = calculationState

    fun updateToken(newToken: Token.Active) {
        if (::token.isInitialized && newToken.mintAddress == this.token.mintAddress) {
            updateLabels()
        } else {
            this.token = newToken
            if (currencyMode is CurrencyMode.Token) {
                currencyMode = CurrencyMode.Token(newToken)
            }

            handleFractionUpdate(currencyMode)
            updateLabels()
        }
    }

    fun updateInputAmount(newInputAmount: String) {
        this.inputAmount = newInputAmount
        recalculate(inputAmount)
    }

    fun reduceAmount(newInputAmountInToken: BigInteger): BigDecimal {
        val newTokenAmount = newInputAmountInToken.fromLamports(token.decimals)
        val newUsdAmount = newTokenAmount.toUsd(token).orZero()
        val newAmount = if (currencyMode is CurrencyMode.Fiat) newUsdAmount else newTokenAmount

        usdAmount = newUsdAmount
        tokenAmount = newTokenAmount
        inputAmount = newAmount.toString()

        if (currencyMode is CurrencyMode.Fiat) {
            handleCalculateTokenAmountUpdate()
        } else {
            handleCalculateUsdAmountUpdate()
        }

        return newAmount
    }

    fun getCurrentAmountLamports(): BigInteger = tokenAmount.toLamports(token.decimals)

    fun getCurrentAmount(): BigDecimal = tokenAmount

    fun getCurrentAmountUsd(): BigDecimal? = usdAmount.takeIf { token.rate != null }

    fun isCurrentInputEmpty(): Boolean = inputAmount.isEmpty()

    fun getMaxAvailableAmount(): BigDecimal? {
        tokenAmount = token.total
        usdAmount = token.totalInUsdScaled.orZero()

        val maxAmount = when (currencyMode) {
            is CurrencyMode.Fiat -> {
                handleCalculateTokenAmountUpdate()
                token.totalInUsdScaled
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
            is CurrencyMode.Token -> CurrencyMode.Fiat.Usd // only support USD
            is CurrencyMode.Fiat -> CurrencyMode.Token(token)
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
            inputAmount.isEmpty() || amountInLamports >= maxAllowedAmount && amountInLamports < token.totalInLamports
        } else {
            inputAmount.isEmpty()
        }
    }

    private fun updateLabels() {
        val (switchSymbol, mainSymbol) = when (val mode = currencyMode) {
            is CurrencyMode.Token -> USD_READABLE_SYMBOL to token.tokenSymbol
            is CurrencyMode.Fiat -> token.tokenSymbol to mode.fiatAbbreviation
        }

        updateState(LabelsUpdate(switchSymbol, mainSymbol))

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

        val tokenAround = usdAmount.divideSafe(token.usdRateOrZero, token.decimals)
        tokenAmount = tokenAround

        handleCalculateTokenAmountUpdate()
    }

    private fun calculateByToken(inputAmount: String) {
        tokenAmount = inputAmount.toBigDecimalOrZero()
        usdAmount = if (tokenAmount.isZero()) BigDecimal.ZERO else tokenAmount.multiply(token.usdRateOrZero)

        handleCalculateUsdAmountUpdate()
    }

    private fun handleCalculateTokenAmountUpdate() {
        handleCalculationUpdate(tokenAmount.formatToken(token.decimals), token.tokenSymbol)
    }

    private fun handleCalculateUsdAmountUpdate() {
        val formattedUsdAmount = if (usdAmount.lessThenMinValue()) lessThenMinString else usdAmount.formatFiat()
        handleCalculationUpdate(formattedUsdAmount, USD_READABLE_SYMBOL)
    }

    private fun handleFractionUpdate(mode: CurrencyMode) {
        updateState(InputFractionUpdate(currencyMode.fractionLength))
    }

    private fun handleCalculationUpdate(value: String, symbol: String) {
        updateState(CalculationCompleted("$value $symbol"))
    }

    /**
     * For new bridge. Do not call any callback, just update inner amount
     */
    fun updateTokenAmount(newTokenAmount: BigDecimal) {
        val newUsdAmount = newTokenAmount.multiply(token.usdRateOrZero)
        val newAmount = if (currencyMode is CurrencyMode.Fiat) newUsdAmount else newTokenAmount

        usdAmount = newUsdAmount
        tokenAmount = newTokenAmount
        inputAmountDecimal = newAmount
        inputAmount = formatInputAmount

        if (currencyMode is CurrencyMode.Fiat) {
            handleCalculateTokenAmountUpdate()
        } else {
            handleCalculateUsdAmountUpdate()
        }
    }

    /**
     * For new bridge. Do not call any math, just update [currencyMode] and invoke callback. Expect only update UI
     */
    fun switchAndUpdateInputAmount(): String {
        val newMode = when (currencyMode) {
            is CurrencyMode.Token -> CurrencyMode.Fiat.Usd // only support USD
            is CurrencyMode.Fiat -> CurrencyMode.Token(token)
        }

        val (switchSymbol, mainSymbol) = when (newMode) {
            is CurrencyMode.Token -> USD_READABLE_SYMBOL to token.tokenSymbol
            is CurrencyMode.Fiat -> token.tokenSymbol to newMode.fiatAbbreviation
        }

        // update fraction
        handleFractionUpdate(newMode)

        // update labels
        updateState(LabelsUpdate(switchSymbol, mainSymbol))

        // update around value
        when (newMode) {
            is CurrencyMode.Token -> handleCalculateUsdAmountUpdate()
            is CurrencyMode.Fiat -> handleCalculateTokenAmountUpdate()
        }

        inputAmount = when (newMode) {
            is CurrencyMode.Fiat -> usdAmount.toPlainString()
            is CurrencyMode.Token -> tokenAmount.toPlainString()
        }

        currencyMode = newMode
        return inputAmount
    }

    private fun updateState(newState: CalculationState) {
        calculationState.value = newState
    }
}

package org.p2p.wallet.send.model

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.divideSafe
import org.p2p.core.utils.emptyString
import org.p2p.core.utils.formatFiat
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.isZero
import org.p2p.core.utils.lessThenMinValue
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider

private const val TAG = "CalculationMode"

class CalculationMode(
    private val sendModeProvider: SendModeProvider,
    private val lessThenMinString: String,
) {

    var onCalculationCompleted: ((aroundValue: String) -> Unit)? = null
    var onInputFractionUpdated: ((Int) -> Unit)? = null
    var onLabelsUpdated: ((switchSymbol: String, mainSymbol: String) -> Unit)? = null

    private var currencyMode: CurrencyMode = SendModeProvider.EMPTY_TOKEN

    private lateinit var token: Token.Active

    private var inputAmount: String = emptyString()

    var maxTokenAmount: BigDecimal = BigDecimal.ZERO

    var inputAmountDecimal: BigDecimal = BigDecimal.ZERO
        private set

    private var useMax = false

    val formatInputAmount: String
        get() = when (currencyMode) {
            is CurrencyMode.Fiat -> inputAmountDecimal.formatFiat()
            is CurrencyMode.Token -> inputAmountDecimal.formatToken(token.decimals)
        }

    private var enteredTokenAmount: BigDecimal = BigDecimal.ZERO
    private var usdAmount: BigDecimal = BigDecimal.ZERO

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
        enteredTokenAmount = newTokenAmount
        inputAmount = newAmount.toString()

        if (currencyMode is CurrencyMode.Fiat) {
            handleCalculateTokenAmountUpdate()
        } else {
            handleCalculateUsdAmountUpdate()
        }

        return newAmount
    }

    fun getCurrentAmountLamports(): BigInteger = enteredTokenAmount.toLamports(token.decimals)

    fun getCurrentAmount(): BigDecimal = enteredTokenAmount

    fun getCurrentAmountUsd(): BigDecimal? = usdAmount.takeIf { token.rate != null }

    fun isCurrentInputEmpty(): Boolean = inputAmount.isEmpty()

    /**
     * @param calculatedMaxAmountToSend is not null if it's calculated by using send-service
     * if it's null - use old way of doing things, but it can fail due to token.total
     * can't be sent with fees applied upon
     */
    fun setMaxAvailableAmountInInputs(): BigDecimal? {
        if (maxTokenAmount.isZero()) {
            enteredTokenAmount = token.total
            usdAmount = token.totalInUsdScaled.orZero()
        } else {
            enteredTokenAmount = maxTokenAmount
            usdAmount = maxTokenAmount.toUsd(token).orZero()
        }

        val maxAmount: BigDecimal? = when (currencyMode) {
            is CurrencyMode.Fiat -> {
                handleCalculateTokenAmountUpdate()
                token.totalInUsdScaled
            }
            is CurrencyMode.Token -> {
                handleCalculateUsdAmountUpdate()
                maxTokenAmount.takeIf { it.isNotZero() } ?: enteredTokenAmount
            }
        }

        inputAmount = maxAmount.orZero().toPlainString()
        return maxAmount
    }

    fun setMaxAmounts(calculatedMaxAmountToSend: BigDecimal) {
        Timber.e("Setting max amount: $calculatedMaxAmountToSend")
        maxTokenAmount = calculatedMaxAmountToSend
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

    fun getDebugInfo(): String = buildString {
        val remainingBalance = token.totalInLamports - getCurrentAmountLamports()
        append("Remaining balance: $remainingBalance")
    }

    fun isMaxButtonVisible(minRentExemption: BigInteger): Boolean {
        return if (token.isSOL) {
            val maxAllowedAmount = token.totalInLamports - minRentExemption
            val totalSolAmount = token.totalInLamports
            val enteredAmountInLamports = enteredTokenAmount.toLamports(token.decimals)
            inputAmount.isEmpty() ||
                enteredAmountInLamports >= maxAllowedAmount &&
                enteredAmountInLamports < totalSolAmount
        } else {
            inputAmount.isEmpty()
        }
    }

    private fun updateLabels() {
        val (switchSymbol, mainSymbol) = when (val mode = currencyMode) {
            is CurrencyMode.Token -> USD_READABLE_SYMBOL to token.tokenSymbol
            is CurrencyMode.Fiat -> token.tokenSymbol to mode.fiatAbbreviation
        }

        onLabelsUpdated?.invoke(switchSymbol, mainSymbol)

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
        enteredTokenAmount = tokenAround

        handleCalculateTokenAmountUpdate()
    }

    private fun calculateByToken(inputAmount: String) {
        enteredTokenAmount = inputAmount.toBigDecimalOrZero()
        usdAmount = if (enteredTokenAmount.isZero()) {
            BigDecimal.ZERO
        } else {
            enteredTokenAmount.multiply(token.usdRateOrZero)
        }

        handleCalculateUsdAmountUpdate()
    }

    private fun handleCalculateTokenAmountUpdate() {
        handleCalculationUpdate(enteredTokenAmount.formatToken(token.decimals), token.tokenSymbol)
    }

    private fun handleCalculateUsdAmountUpdate() {
        val formattedUsdAmount = if (usdAmount.lessThenMinValue()) lessThenMinString else usdAmount.formatFiat()
        handleCalculationUpdate(formattedUsdAmount, USD_READABLE_SYMBOL)
    }

    private fun handleFractionUpdate(mode: CurrencyMode) {
        onInputFractionUpdated?.invoke(mode.fractionLength)
    }

    private fun handleCalculationUpdate(value: String, symbol: String) {
        onCalculationCompleted?.invoke("$value $symbol")
    }

    /**
     * For new bridge. Do not call any callback, just update inner amount
     */
    fun updateTokenAmount(newTokenAmount: BigDecimal) {
        val newUsdAmount = newTokenAmount.multiply(token.usdRateOrZero)
        val newAmount = if (currencyMode is CurrencyMode.Fiat) newUsdAmount else newTokenAmount

        usdAmount = newUsdAmount
        enteredTokenAmount = newTokenAmount
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
        onLabelsUpdated?.invoke(switchSymbol, mainSymbol)

        // update around value
        when (newMode) {
            is CurrencyMode.Token -> handleCalculateUsdAmountUpdate()
            is CurrencyMode.Fiat -> handleCalculateTokenAmountUpdate()
        }

        inputAmount = when (newMode) {
            is CurrencyMode.Fiat -> usdAmount.toPlainString()
            is CurrencyMode.Token -> enteredTokenAmount.toPlainString()
        }

        currencyMode = newMode
        return inputAmount
    }

    fun setUseMax(value: Boolean) {
        useMax = value
    }

    fun isMaxUsed(): Boolean = useMax
}

package org.p2p.wallet.newsend.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import android.content.res.Resources
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toLamports
import org.p2p.wallet.R
import org.p2p.wallet.send.model.CurrencyMode
import org.p2p.wallet.send.model.SearchResult
import java.math.BigInteger
import kotlinx.parcelize.IgnoredOnParcel

class NewSendButtonValidator(
    private val sourceToken: Token.Active,
    private val searchResult: SearchResult,
    private val feeRelayerState: FeeRelayerState,
    private val calculationMode: CalculationMode,
    private val minRentExemption: BigInteger,
    private val resources: Resources
) {

    sealed interface State {
        class Disabled(
            val textContainer: TextContainer,
            @ColorRes val totalAmountTextColor: Int
        ) : State

        class Enabled(
            @StringRes val textResId: Int,
            val value: String,
            @ColorRes val totalAmountTextColor: Int
        ) : State
    }

    @IgnoredOnParcel
    val state: State
        get() {
            val total = sourceToken.total.toLamports(sourceToken.decimals)
            val inputAmount = calculationMode.getCurrentAmountLamports()

            val isEnoughBalance = !total.isLessThan(inputAmount)
            val isFeeCalculationValid = feeRelayerState.isValidState()

            val sendFee = (feeRelayerState as? FeeRelayerState.UpdateFee)?.solanaFee
            val isEnoughToCoverExpenses = sendFee == null || sendFee.isEnoughToCoverExpenses(total, inputAmount)
            val isAmountNotZero = inputAmount.isNotZero()
            val isAmountValidForRecipient = isAmountValidForRecipient(inputAmount)
            val isAmountValidForSender = isAmountValidForSender(inputAmount)

            val inputTextColor = when {
                !isEnoughBalance -> R.color.text_rose
                !isEnoughToCoverExpenses -> R.color.text_rose
                else -> R.color.text_night
            }

            return when {
                !isAmountNotZero -> {
                    val textContainer = TextContainer.Res(R.string.main_enter_the_amount)
                    State.Disabled(textContainer, inputTextColor)
                }
                !isFeeCalculationValid -> {
                    val textContainer = TextContainer.Res(R.string.send_cant_calculate_fees_error)
                    State.Disabled(textContainer, inputTextColor)
                }
                !isEnoughBalance -> {
                    val tokenSymbol = sourceToken.tokenSymbol
                    val textResFormat = R.string.send_max_warning_text_format
                    val currentMode = calculationMode.getCurrentMode()
                    val text = if (currentMode is CurrencyMode.Usd && sourceToken.totalInUsd != null) {
                        resources.getString(textResFormat, sourceToken.totalInUsd, USD_READABLE_SYMBOL)
                    } else {
                        resources.getString(textResFormat, sourceToken.total.toPlainString(), tokenSymbol)
                    }
                    val textContainer = TextContainer.Raw(text)
                    State.Disabled(textContainer, inputTextColor)
                }
                !isEnoughToCoverExpenses -> {
                    val textContainer = TextContainer.Res(R.string.send_insufficient_funds)
                    State.Disabled(textContainer, inputTextColor)
                }
                !isAmountValidForRecipient -> {
                    val solAmount = minRentExemption.fromLamports().scaleLong().toPlainString()
                    val format = resources.getString(R.string.send_min_warning_text_format, solAmount, SOL_SYMBOL)
                    State.Disabled(
                        textContainer = TextContainer.Raw(format),
                        totalAmountTextColor = inputTextColor
                    )
                }
                !isAmountValidForSender -> {
                    val solAmount = minRentExemption.fromLamports().scaleLong().toPlainString()
                    val format = resources.getString(R.string.send_min_required_user_balance, solAmount, SOL_SYMBOL)
                    State.Disabled(
                        textContainer = TextContainer.Raw(format),
                        totalAmountTextColor = inputTextColor
                    )
                }
                else -> {
                    val valueText = calculationMode.getValueByMode().toPlainString()
                    val symbol = calculationMode.getSymbolByMode()
                    State.Enabled(
                        textResId = R.string.send_format,
                        value = "$valueText $symbol",
                        totalAmountTextColor = inputTextColor
                    )
                }
            }
        }

    /**
     * This case is only for sending SOL
     *
     * 1. The recipient and sender should have at least [minRentExemption] SOL balance
     * 2. The sender is allowed to sent exactly the whole SOL balance.
     * It's allowed for the sender to have a SOL balance 0 or at least [minRentExemption]
     * */
    private fun isAmountValidForRecipient(amount: BigInteger): Boolean {
        val isSourceTokenSol = sourceToken.isSOL
        val isRecipientEmpty = searchResult is SearchResult.EmptyBalance

        val isInputValidForRecipient = amount >= minRentExemption
        if (!isSourceTokenSol) return true

        val isInvalid = isRecipientEmpty && !isInputValidForRecipient
        return !isInvalid
    }

    /**
     * This case is only for sending SOL
     *
     * 1. The recipient and sender should have at least [minRentExemption] balance
     * 2. The sender is allowed to sent exactly the whole balance.
     * It's allowed for the sender to have a SOL balance 0 or at least [minRentExemption]
     * */
    private fun isAmountValidForSender(amount: BigInteger): Boolean {
        val isSourceTokenSol = sourceToken.isSOL
        val balanceDiff = sourceToken.totalInLamports - amount

        val isValidRemainingSource = balanceDiff.isZero() || balanceDiff > minRentExemption
        if (!isSourceTokenSol) return true

        return isValidRemainingSource
    }
}

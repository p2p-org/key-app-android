package org.p2p.wallet.newsend.model

import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import kotlinx.parcelize.IgnoredOnParcel
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.toLamports
import org.p2p.wallet.R
import org.p2p.wallet.send.model.CurrencyMode
import org.p2p.wallet.send.model.SearchResult
import java.math.BigInteger

class NewSendButton(
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
                    State.Disabled(
                        textContainer = TextContainer.Res(R.string.send_min_required_amount_warning),
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

    private fun isAmountValidForRecipient(amount: BigInteger): Boolean {
        val isSourceTokenSol = sourceToken.isSOL
        val isRecipientEmpty = searchResult is SearchResult.EmptyBalance
        val isInputInvalid = amount < minRentExemption
        val isInvalid = isSourceTokenSol && isRecipientEmpty && isInputInvalid
        return !isInvalid
    }
}

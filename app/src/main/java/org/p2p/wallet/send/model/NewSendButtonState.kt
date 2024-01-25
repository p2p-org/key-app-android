package org.p2p.wallet.send.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import android.content.res.Resources
import java.math.BigInteger
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toLamports
import org.p2p.wallet.R

class NewSendButtonState(
    private val tokenToSend: Token.Active,
    private val recipient: SearchResult,
    private val feeRelayerState: FeeRelayerState,
    private val calculationMode: CalculationMode,
    private val minRentExemption: BigInteger,
    private val resources: Resources
) {

    private val minSolValidator = NewSendButtonStateMinSolValidator(
        tokenToSend = tokenToSend, minRentExemption = minRentExemption, recipient = recipient
    )

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

    fun getCurrentState(): State {
        // get maxAmount from calculationMode
        // where calculationMode.maxAmount is set from click on button "MAX"
        // which sets calculationMode.maxAmount using SendMaximumAmountCalculator
        val maxAmountToSendLamports = calculationMode.maxTokenAmount.toLamports(tokenToSend.decimals)
        val inputAmount = calculationMode.getCurrentAmountLamports()

        val isNotEnoughBalance = maxAmountToSendLamports.isLessThan(inputAmount)
        val isFeeCalculationInvalid = !feeRelayerState.isValidState()

        val sendFee = (feeRelayerState as? FeeRelayerState.UpdateFee)?.solanaFee
        val isEnoughToCoverExpenses = sendFee == null || sendFee.isEnoughToCoverExpenses(
            sourceTokenTotal = maxAmountToSendLamports,
            inputAmount = inputAmount,
            minRentExemption = minRentExemption
        )
        val isAmountZero = inputAmount.isZero()
        val isAmountValidForRecipient = minSolValidator.isAmountValidForRecipient(inputAmount)
        val isAmountValidForSender = minSolValidator.isAmountValidForSender(inputAmount)
        val isMinRequiredBalanceLeft = minSolValidator.isMinRequiredBalanceLeft(inputAmount)

        return when {
            isFeeCalculationInvalid -> {
                val textContainer = TextContainer.Res(R.string.send_cant_calculate_fees_error)
                State.Disabled(textContainer, R.color.text_night)
            }
            isAmountZero -> {
                val textContainer = TextContainer.Res(R.string.send_enter_amount)
                State.Disabled(textContainer, R.color.text_night)
            }
            isNotEnoughBalance -> {
                val tokenSymbol = tokenToSend.tokenSymbol
                val totalAllowed = maxAmountToSendLamports
                    .fromLamports(tokenToSend.decimals)
                    .formatToken(tokenToSend.decimals)
                val textResFormat = R.string.send_max_warning_text_format
                val text = resources.getString(textResFormat, totalAllowed, tokenSymbol)
                val textContainer = TextContainer.Raw(text)
                State.Disabled(textContainer, R.color.text_rose)
            }
            !isEnoughToCoverExpenses -> {
                val textContainer = TextContainer.Res(R.string.send_insufficient_funds)
                State.Disabled(textContainer, R.color.text_rose)
            }
            !isMinRequiredBalanceLeft -> {
                val textContainer = TextContainer.Res(R.string.error_insufficient_funds)
                State.Disabled(textContainer, R.color.text_rose)
            }
            !isAmountValidForRecipient -> {
                val solAmount = minRentExemption.fromLamports().scaleLong().toPlainString()
                val format = resources.getString(R.string.send_min_warning_text_format, solAmount, SOL_SYMBOL)
                State.Disabled(
                    textContainer = TextContainer.Raw(format),
                    totalAmountTextColor = R.color.text_rose
                )
            }
            !isAmountValidForSender -> {
                val maxSolAmountAllowed = tokenToSend.totalInLamports - minRentExemption
                val format = resources.getString(
                    R.string.send_max_warning_text_format,
                    maxSolAmountAllowed.fromLamports().scaleLong().toPlainString(),
                    SOL_SYMBOL
                )
                State.Disabled(
                    textContainer = TextContainer.Raw(format),
                    totalAmountTextColor = R.color.text_rose
                )
            }
            else -> {
                State.Enabled(
                    textResId = R.string.send_format,
                    value = "${calculationMode.getCurrentAmount().toPlainString()} ${tokenToSend.tokenSymbol}",
                    totalAmountTextColor = R.color.text_night
                )
            }
        }
    }
}

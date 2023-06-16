package org.p2p.wallet.newsend.smartselection

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import android.content.res.Resources
import java.math.BigDecimal
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.R
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.FeePayerState.CalculationSuccess
import org.p2p.wallet.newsend.model.FeePayerState.Failure
import org.p2p.wallet.newsend.model.FeePayerState.FreeTransaction
import org.p2p.wallet.newsend.model.FeePayerState.Idle
import org.p2p.wallet.newsend.model.FeePayerState.NoStrategiesFound
import org.p2p.wallet.newsend.model.FeePayerState.ReduceAmount
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason.CalculationError
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason.ExceededFee
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason.InputExceeded
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason.InvalidAmountForRecipient
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason.InvalidAmountForSender
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason.LowMinBalanceIgnored

/**
 * According to [FeePayerState] we show the certain state for action button in Send.
 * It can be enabled and show the amount which can be sent
 * Or it can be disabled and show the reason why it is disabled
 * */
class SendButtonMapper(
    private val resources: Resources
) {

    fun mapToButtonState(state: FeePayerState): State = when (state) {
        is Idle -> createEnterAmountState()
        is NoStrategiesFound -> createCalculationErrorState()
        is FreeTransaction -> createEnabledOrEnterAmountState(state.initialAmount, state.formattedAmount)
        is CalculationSuccess -> createEnabledOrEnterAmountState(state.inputAmount, state.formattedAmount)
        is ReduceAmount -> createEnabledState(state.formattedAmount)
        is Failure -> handleFailureReason(state.reason)
    }

    private fun handleFailureReason(reason: FeePayerFailureReason): State =
        when (reason) {
            is CalculationError -> createCalculationErrorState()
            is InputExceeded -> createInputExceededState(reason)
            is ExceededFee -> createExceededFeeState()
            is LowMinBalanceIgnored -> createInsufficientFundsState()
            is InvalidAmountForRecipient -> createInvalidAmountForRecipientState(reason)
            is InvalidAmountForSender -> createInvalidAmountForSenderState(reason)
        }

    private fun createEnabledOrEnterAmountState(amount: BigDecimal?, formattedAmount: String): State =
        if (amount == null) {
            createEnterAmountState()
        } else {
            createEnabledState(formattedAmount)
        }

    private fun createInvalidAmountForSenderState(state: InvalidAmountForSender): State.Disabled {
        val format = resources.getString(
            R.string.send_max_warning_text_format, state.maxSolAmountAllowed, SOL_SYMBOL
        )
        return State.Disabled(
            textContainer = TextContainer.Raw(format), totalAmountTextColor = R.color.text_rose
        )
    }

    private fun createInvalidAmountForRecipientState(state: InvalidAmountForRecipient): State.Disabled {
        val solAmount = state.minRequiredSolBalance
        val format = resources.getString(R.string.send_min_warning_text_format, solAmount, SOL_SYMBOL)
        return State.Disabled(
            textContainer = TextContainer.Raw(format), totalAmountTextColor = R.color.text_rose
        )
    }

    private fun createInsufficientFundsState(): State.Disabled {
        val textContainer = TextContainer.Res(R.string.error_insufficient_funds)
        return State.Disabled(textContainer, R.color.text_rose)
    }

    private fun createExceededFeeState(): State.Disabled {
        val textContainer = TextContainer.Res(R.string.send_insufficient_funds)
        return State.Disabled(textContainer, R.color.text_rose)
    }

    private fun createInputExceededState(state: InputExceeded): State.Disabled {
        val tokenSymbol = state.sourceToken.tokenSymbol
        val textResFormat = R.string.send_max_warning_text_format
        val text = resources.getString(textResFormat, state.sourceToken.total.toPlainString(), tokenSymbol)
        val textContainer = TextContainer.Raw(text)
        return State.Disabled(textContainer, R.color.text_rose)
    }

    private fun createEnterAmountState(): State.Disabled {
        val textContainer = TextContainer.Res(R.string.send_enter_amount)
        return State.Disabled(textContainer, R.color.text_night)
    }

    private fun createCalculationErrorState(): State.Disabled {
        val textContainer = TextContainer.Res(R.string.send_cant_calculate_fees_error)
        return State.Disabled(textContainer, R.color.text_night)
    }

    private fun createEnabledState(formattedInput: String): State.Enabled = State.Enabled(
        textResId = R.string.send_format,
        value = formattedInput,
        totalAmountTextColor = R.color.text_night
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
}

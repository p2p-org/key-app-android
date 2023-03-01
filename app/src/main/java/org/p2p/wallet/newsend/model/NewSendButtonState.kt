package org.p2p.wallet.newsend.model

import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleLong
import org.p2p.wallet.R
import java.math.BigInteger

class NewSendButtonState(
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

    val currentState: State
        get() {
            val totalInLamports = sourceToken.totalInLamports
            val inputAmount = calculationMode.getCurrentAmountLamports()

            val isEnoughBalance = !totalInLamports.isLessThan(inputAmount)
            val isFeeCalculationValid = feeRelayerState.isValidState()

            val sendFee = (feeRelayerState as? FeeRelayerState.UpdateFee)?.solanaFee
            val isEnoughToCoverExpenses =
                sendFee == null || sendFee.isEnoughToCoverExpenses(totalInLamports, inputAmount, minRentExemption)
            val isAmountNotZero = inputAmount.isNotZero()
            val isAmountValidForRecipient = isAmountValidForRecipient(inputAmount)
            val isAmountValidForSender = isAmountValidForSender(inputAmount)
            val isMinRequiredBalanceLeft = isMinRequiredBalanceLeft()

            return when {
                !isFeeCalculationValid -> {
                    val textContainer = TextContainer.Res(R.string.send_cant_calculate_fees_error)
                    State.Disabled(textContainer, R.color.text_night)
                }
                !isAmountNotZero -> {
                    val textContainer = TextContainer.Res(R.string.main_enter_the_amount)
                    State.Disabled(textContainer, R.color.text_night)
                }
                !isEnoughBalance -> {
                    val tokenSymbol = sourceToken.tokenSymbol
                    val textResFormat = R.string.send_max_warning_text_format
                    val text = resources.getString(textResFormat, sourceToken.total.toPlainString(), tokenSymbol)
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
                    val maxSolAmountAllowed = sourceToken.totalInLamports - minRentExemption
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
                        value = "${calculationMode.getCurrentAmount().toPlainString()} ${sourceToken.tokenSymbol}",
                        totalAmountTextColor = R.color.text_night
                    )
                }
            }
        }

    /**
     * This case is only for sending SOL
     *
     * 1. The recipient should receive at least [minRentExemption] SOL balance if his current balance is 0
     * 2. The recipient should have at least [minRentExemption] after the transaction
     * */
    private fun isAmountValidForRecipient(amount: BigInteger): Boolean {
        val isSourceTokenSol = sourceToken.isSOL
        val isRecipientEmpty = searchResult is SearchResult.AddressFound && searchResult.isEmptyBalance

        val isInputValidForRecipient = amount >= minRentExemption
        if (!isSourceTokenSol) return true

        val isInvalid = isRecipientEmpty && !isInputValidForRecipient
        return !isInvalid
    }

    /**
     * This case is only for sending SOL
     *
     * 1. The sender is allowed to sent exactly the whole balance.
     * 2. It's allowed for the sender to have a SOL balance 0 or at least [minRentExemption]
     * */
    private fun isAmountValidForSender(amount: BigInteger): Boolean {
        val isSourceTokenSol = sourceToken.isSOL
        val balanceDiff = sourceToken.totalInLamports - amount

        val isValidRemainingSource = balanceDiff.isZero() || balanceDiff >= minRentExemption
        if (!isSourceTokenSol) return true

        return isValidRemainingSource
    }

    /**
     * Validating only SOL -> SOL operations here
     * The empty recipient is required
     * Checking if the sender should leave at least [minRentExemption] or Zero SOL balance
     * */
    private fun isMinRequiredBalanceLeft(): Boolean {
        if (!sourceToken.isSOL) return true

        val isRecipientEmpty = searchResult is SearchResult.AddressFound && searchResult.isEmptyBalance
        if (!isRecipientEmpty) return true

        val sourceTotalLamports = sourceToken.totalInLamports
        val minRequiredBalance = minRentExemption

        val inputAmountInLamports = calculationMode.getCurrentAmountLamports()
        val diff = sourceTotalLamports - inputAmountInLamports

        return diff == BigInteger.ZERO || diff >= minRequiredBalance
    }
}

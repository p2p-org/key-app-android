package org.p2p.wallet.newsend.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.core.token.Token
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.toLamports
import org.p2p.wallet.R
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendSolanaFee
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.parcelize.IgnoredOnParcel

class NewSendButton(
    private val sourceToken: Token.Active,
    private val searchResult: SearchResult?,
    private val sendFee: SendSolanaFee?,
    private val tokenAmount: BigDecimal,
    private var minRentExemption: BigInteger
) {

    sealed interface State {
        class Disabled(
            @StringRes val textResId: Int,
            @ColorRes val totalAmountTextColor: Int
        ) : State

        class Enabled(
            @StringRes val textResId: Int,
            @DrawableRes val iconRes: Int?,
            vararg val value: String,
            @ColorRes val totalAmountTextColor: Int
        ) : State
    }

    @IgnoredOnParcel
    val state: State
        get() {
            val total = sourceToken.total.toLamports(sourceToken.decimals)
            val inputAmount = tokenAmount.toLamports(sourceToken.decimals)

            val isEnoughBalance = !total.isLessThan(inputAmount)
            val isEnoughToCoverExpenses = sendFee == null || sendFee.isEnoughToCoverExpenses(total, inputAmount)
            val isAmountNotZero = inputAmount.isNotZero()
            val isAmountValidForRecipient = isAmountValidForRecipient(inputAmount)

            val availableColor = when {
                !isEnoughBalance -> R.color.text_rose
                else -> R.color.text_night
            }

            return when {
                !isAmountNotZero ->
                    State.Disabled(R.string.main_enter_the_amount, availableColor)
                !isEnoughBalance ->
                    State.Disabled(R.string.swap_funds_not_enough, availableColor)
                !isEnoughToCoverExpenses ->
                    State.Disabled(R.string.send_insufficient_funds, availableColor)
                !isAmountValidForRecipient ->
                    State.Disabled(
                        textResId = R.string.send_min_required_amount_warning,
                        totalAmountTextColor = availableColor
                    )
                else ->
                    State.Enabled(
                        textResId = R.string.send_format,
                        iconRes = R.drawable.ic_send_simple,
                        value = arrayOf("${tokenAmount.toPlainString()} ${sourceToken.tokenSymbol}"),
                        totalAmountTextColor = availableColor
                    )
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

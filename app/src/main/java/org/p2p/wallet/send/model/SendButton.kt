package org.p2p.wallet.send.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.IgnoredOnParcel
import org.p2p.solanaj.utils.PublicKeyValidator
import org.p2p.wallet.R
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.isLessThan
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.toLamports
import java.math.BigDecimal
import java.math.BigInteger

class SendButton(
    private val sourceToken: Token.Active,
    private val searchResult: SearchResult?,
    private val sendFee: SendFee?,
    private val tokenAmount: BigDecimal,
    private var minRentExemption: BigInteger
) {

    sealed class State {
        class Disabled(
            @StringRes val textResId: Int,
            @ColorRes val totalAmountTextColor: Int,
            @StringRes val warningTextResId: Int? = null
        ) : State()

        class Enabled(
            @StringRes val textResId: Int,
            @DrawableRes val iconRes: Int?,
            vararg val value: String,
            @ColorRes val totalAmountTextColor: Int
        ) : State()
    }

    @IgnoredOnParcel
    val state: State
        get() {
            val total = sourceToken.total.toLamports(sourceToken.decimals)
            val inputAmount = tokenAmount.toLamports(sourceToken.decimals)
            val address = searchResult?.searchAddress?.address.orEmpty()

            val isEnoughBalance = inputAmount.isLessThan(total)
            val isEnoughToCoverExpenses = sendFee != null && sendFee.isEnoughToCoverExpenses(total, inputAmount)
            val isValidAddress = PublicKeyValidator.isValid(address)
            val isAmountNotZero = inputAmount.isNotZero()
            val isAmountValidForRecipient = isAmountValidForRecipient(inputAmount)

            val isMaxAmount = inputAmount == total
            val availableColor = when {
                !isEnoughBalance -> R.color.systemErrorMain
                isMaxAmount -> R.color.systemSuccessMain
                else -> R.color.textIconSecondary
            }

            return when {
                !isValidAddress ->
                    State.Disabled(R.string.send_enter_address, availableColor)
                !isAmountNotZero ->
                    State.Disabled(R.string.main_enter_the_amount, availableColor)
                !isEnoughBalance ->
                    State.Disabled(R.string.swap_funds_not_enough, availableColor)
                !isEnoughToCoverExpenses ->
                    State.Disabled(R.string.send_insufficient_funds, availableColor)
                !isAmountValidForRecipient ->
                    State.Disabled(
                        textResId = R.string.main_enter_incorrect_amount,
                        totalAmountTextColor = availableColor,
                        warningTextResId = R.string.send_min_required_amount_warning
                    )
                else ->
                    State.Enabled(
                        textResId = R.string.send_format,
                        iconRes = R.drawable.ic_send_simple,
                        value = arrayOf("$tokenAmount ${sourceToken.tokenSymbol}"),
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

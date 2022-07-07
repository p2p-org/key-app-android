package org.p2p.wallet.swap.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.R
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.SwapFee
import org.p2p.wallet.utils.isLessThan
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.toBigDecimalOrZero
import org.p2p.wallet.utils.toLamports

class SwapButton(
    private val bestPoolPair: OrcaPoolsPair?,
    private val sourceAmount: String,
    private val sourceToken: Token.Active,
    private val destinationToken: Token?,
    private val swapFee: SwapFee?
) {

    sealed class State {
        class Disabled(@StringRes val textResId: Int) : State()

        class Enabled(
            @StringRes val textRes: Int,
            @DrawableRes val iconRes: Int? = null,
            vararg val value: String
        ) : State()
    }

    val state: State
        get() {
            val isPairEmpty = bestPoolPair.isNullOrEmpty()
            val inputAmount = sourceAmount.toBigDecimalOrZero().toLamports(sourceToken.decimals)
            val total = sourceToken.total.toLamports(sourceToken.decimals)
            val isEnoughBalance = !total.isLessThan(inputAmount)

            val isEnoughToCoverExpenses = swapFee == null || swapFee.isEnoughToCoverExpenses(total, inputAmount)
            val isValidAmount = inputAmount.isNotZero()

            val isValidDestination = destinationToken != null

            return when {
                !isValidAmount -> State.Disabled(R.string.main_enter_the_amount)
                !isValidDestination -> State.Disabled(R.string.swap_choose_the_destination)
                isPairEmpty -> State.Disabled(R.string.swap_cannot_swap_these_tokens)
                !isEnoughBalance -> State.Disabled(R.string.swap_funds_not_enough)
                !isEnoughToCoverExpenses -> State.Disabled(R.string.swap_insufficient_funds)
                else -> {
                    State.Enabled(
                        textRes = R.string.swap_format,
                        iconRes = R.drawable.ic_swap_simple,
                        sourceToken.tokenSymbol,
                        destinationToken?.tokenSymbol.orEmpty()
                    )
                }
            }
        }
}

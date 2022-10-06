package org.p2p.wallet.common.ui.widget.earnwidget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.uikit.glide.GlideManager
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemDepositTokenBinding
import org.p2p.wallet.databinding.WidgetEarnViewBinding
import org.p2p.wallet.utils.DecimalFormatter
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.scaleLong
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import java.math.BigDecimal

private const val DEFAULT_BUTTON_TEXT_PADDING_DP = 20
private const val SMALL_BUTTON_TEXT_PADDING_DP = 16

private const val TOKEN_MARGIN_END_DP = 8

private const val TICKER_DECIMALS = 12

class EarnWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent {

    private val binding: WidgetEarnViewBinding = inflateViewBinding()

    private val glideManager: GlideManager by inject()

    var currentState: EarnWidgetState = EarnWidgetState.Idle

    init {
        binding.tickerViewAmount.apply {
            setCharacterLists(TickerUtils.provideNumberList())
            setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN)
        }
    }

    fun setOnButtonClickListener(buttonClickListener: OnClickListener) {
        binding.viewEarnContent.setOnClickListener(buttonClickListener)
    }

    fun setWidgetState(state: EarnWidgetState) = with(binding) {
        currentState = state

        setWidgetViewsVisibility(state)

        viewEarnContent.setBackgroundColor(getColor(state.backgroundColor))
        when (state) {
            is EarnWidgetState.Balance -> state.showBalanceState()
            is EarnWidgetState.LearnMore -> state.showLearnMoreState()
            is EarnWidgetState.Depositing -> state.showDepositingState()
            is EarnWidgetState.DepositFoundsFailed -> state.showDepositFoundsFailedState()
            is EarnWidgetState.Error -> state.showErrorState()
            else -> showIdleState()
        }
    }

    private fun EarnWidgetState.Balance.showBalanceState() = with(binding) {
        makeAlignStartContent()
        tickerViewAmount.text = "$${amount.scaleLong().formatTicker()}"
        textViewEarnTitle.setText(R.string.earn_widget_balance_title)
        buttonEarn.setText(R.string.earn_widget_balance_button)
        setDepositTokens(tokenIcons)
    }

    private fun EarnWidgetState.LearnMore.showLearnMoreState() = with(binding) {
        makeAlignCenterContent()
        textViewEarnTitle.setText(R.string.earn_widget_learn_more_title)
        textViewEarnMessage.setText(R.string.earn_widget_learn_more_message)
        buttonEarn.setText(R.string.earn_widget_learn_more_button)
    }

    private fun EarnWidgetState.Depositing.showDepositingState() = with(binding) {
        makeAlignStartContent()
        buttonEarn.setText(buttonTextRes)
    }

    private fun EarnWidgetState.DepositFoundsFailed.showDepositFoundsFailedState() = with(binding) {
        makeAlignCenterContent()
        textViewEarnTitle.setText(R.string.earn_widget_deposit_failed_title)
        textViewEarnMessage.setText(R.string.earn_widget_deposit_failed_message)
        buttonEarn.setText(R.string.earn_widget_deposit_failed_button)
    }

    private fun EarnWidgetState.Error.showErrorState() = with(binding) {
        makeAlignCenterContent()
        textViewEarnTitle.setText(R.string.earn_widget_error_title)
        textViewEarnMessage.setText(messageTextRes)
        buttonEarn.setText(buttonTextRes)
    }

    private fun showIdleState() = with(binding) {
        makeAlignCenterContent()
        textViewEarnTitle.isVisible = false
        textViewEarnMessage.isVisible = false
    }

    private fun setWidgetViewsVisibility(state: EarnWidgetState) = with(binding) {
        val isBalanceState = state is EarnWidgetState.Balance
        val isDepositingState = state is EarnWidgetState.Depositing
        val isIdleState = state is EarnWidgetState.Idle

        tickerViewAmount.isVisible = isBalanceState
        viewTokenContainer.isVisible = isBalanceState
        textViewEarnMessage.isVisible = !isBalanceState && !isDepositingState

        textViewEarnTitle.isVisible = !isDepositingState

        shimmerViewTitle.isVisible = isIdleState
        shimmerViewSubTitle.isVisible = isIdleState

        buttonEarn.apply {
            isVisible = !isIdleState
            isEnabled = !isDepositingState
        }
    }

    private fun makeAlignStartContent() = with(binding) {
        textViewEarnTitle.apply {
            textAlignment = TEXT_ALIGNMENT_TEXT_START
            setTextAppearance(R.style.UiKit_TextAppearance_Regular_Text3)
        }
        val horizontalPadding = SMALL_BUTTON_TEXT_PADDING_DP.toPx()
        buttonEarn.apply {
            textAlignment = TEXT_ALIGNMENT_TEXT_START
            setPadding(horizontalPadding, 0, horizontalPadding, 0)
        }
    }

    private fun makeAlignCenterContent() = with(binding) {
        textViewEarnTitle.apply {
            textAlignment = TEXT_ALIGNMENT_CENTER
            setTextAppearance(R.style.UiKit_TextAppearance_SemiBold_Text1)
        }
        val horizontalPadding = DEFAULT_BUTTON_TEXT_PADDING_DP.toPx()
        buttonEarn.apply {
            textAlignment = TEXT_ALIGNMENT_CENTER
            setPadding(horizontalPadding, 0, horizontalPadding, 0)
        }
    }

    private fun setDepositTokens(tokensIcons: List<String>) {
        val container = binding.viewTokenContainer
        if (container.childCount < tokensIcons.size) {
            for (i in container.childCount until tokensIcons.size) {
                val binding = container.inflateViewBinding<ItemDepositTokenBinding>().apply {
                    val params = tokenImageView.layoutParams as MarginLayoutParams
                    params.marginEnd = (TOKEN_MARGIN_END_DP * i).toPx()
                    tokenImageView.layoutParams = params
                }
                val holder = DepositTokenViewHolder(binding, glideManager)
                binding.root.tag = holder
            }
        } else if (container.childCount > tokensIcons.size) {
            if (tokensIcons.isEmpty()) {
                removeAllViews()
            } else {
                container.removeViewsInLayout(
                    tokensIcons.size, container.childCount - tokensIcons.size
                )
            }
        }
        for (i in 0 until container.childCount) {
            (container.getChildAt(i)?.tag as? DepositTokenViewHolder)?.apply {
                bind(tokensIcons[i])
            }
        }
    }
}

private fun BigDecimal.formatTicker(): String = this.run {
    if (isZero()) this.toString() else DecimalFormatter.format(this, TICKER_DECIMALS)
}

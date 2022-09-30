package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
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

    var currentState: EarnWidgetState? = null

    init {
        binding.tickerViewAmount.apply {
            setCharacterLists(TickerUtils.provideNumberList())
            setPreferredScrollingDirection(TickerView.ScrollingDirection.DOWN)
        }
    }

    fun setOnButtonClickListener(buttonClickListener: OnClickListener) {
        binding.buttonEarn.setOnClickListener(buttonClickListener)
    }

    fun setWidgetState(state: EarnWidgetState) = with(binding) {
        currentState = state
        viewEarnContent.setBackgroundColor(getColor(state.backgroundColor))
        val balanceState = state is EarnWidgetState.Balance
        val depositingState = state is EarnWidgetState.Depositing
        textViewEarnTitle.isVisible = !depositingState
        tickerViewAmount.isVisible = balanceState
        textViewEarnMessage.isVisible = !balanceState
        viewTokenContainer.isVisible = balanceState
        buttonEarn.isEnabled = !depositingState
        when (state) {
            is EarnWidgetState.Balance -> {
                makeAlignStartContent()
                tickerViewAmount.text = "$${state.amount.formatTicker()}"
                textViewEarnTitle.setText(R.string.earn_widget_balance_title)
                buttonEarn.setText(R.string.earn_widget_balance_button)
            }
            is EarnWidgetState.LearnMore -> {
                makeAlignCenterContent()
                textViewEarnTitle.setText(R.string.earn_widget_learn_more_title)
                textViewEarnMessage.setText(R.string.earn_widget_learn_more_message)
                buttonEarn.setText(R.string.earn_widget_learn_more_button)
            }
            is EarnWidgetState.Depositing -> {
                makeAlignStartContent()
                tickerViewAmount.isVisible = false
                textViewEarnTitle.isVisible = false
                textViewEarnMessage.isVisible = false
                buttonEarn.setText(state.buttonTextRes)
                buttonEarn.isEnabled = true // TODO remove after tests and real integration
            }
            is EarnWidgetState.DepositFoundsFailed -> {
                makeAlignCenterContent()
                textViewEarnTitle.setText(R.string.earn_widget_deposit_failed_title)
                textViewEarnMessage.setText(R.string.earn_widget_deposit_failed_message)
                buttonEarn.setText(R.string.earn_widget_deposit_failed_button)
            }
            is EarnWidgetState.Error -> {
                makeAlignCenterContent()
                textViewEarnTitle.setText(R.string.earn_widget_error_title)
                textViewEarnMessage.setText(state.messageTextRes)
                buttonEarn.setText(state.buttonTextRes)
            }
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

    fun setDepositTokens(tokens: List<String>) {
        val container = binding.viewTokenContainer
        if (container.childCount < tokens.size) {
            for (i in container.childCount until tokens.size) {
                val binding = ItemDepositTokenBinding.inflate(
                    LayoutInflater.from(context), container, true
                ).apply {
                    val params = tokenImageView.layoutParams as MarginLayoutParams
                    params.marginEnd = (TOKEN_MARGIN_END_DP * i).toPx()
                    tokenImageView.layoutParams = params
                }
                val holder = DepositTokenViewHolder(binding, glideManager)
                binding.root.tag = holder
            }
        } else if (container.childCount > tokens.size) {
            if (tokens.isEmpty()) {
                removeAllViews()
            } else {
                container.removeViewsInLayout(
                    tokens.size, container.childCount - tokens.size
                )
            }
        }
        for (i in 0 until container.childCount) {
            (container.getChildAt(i)?.tag as? DepositTokenViewHolder)?.apply {
                bind(tokens[i])
            }
        }
    }
}

class DepositTokenViewHolder(
    private val binding: ItemDepositTokenBinding,
    private val glideManager: GlideManager
) {

    companion object {
        private const val IMAGE_SIZE = 16
    }

    fun bind(tokenUrl: String) = with(binding) {
        glideManager.load(tokenImageView, tokenUrl, IMAGE_SIZE)
    }
}

fun BigDecimal.formatTicker(): String = this.run {
    if (isZero()) this.toString() else DecimalFormatter.format(this, TICKER_DECIMALS)
}

sealed class EarnWidgetState(@ColorRes val backgroundColor: Int = R.color.bg_rain) {
    object LearnMore : EarnWidgetState(backgroundColor = R.color.bg_lime)
    data class Depositing(@StringRes val buttonTextRes: Int) : EarnWidgetState()
    object DepositFoundsFailed : EarnWidgetState()
    data class Balance(val amount: BigDecimal) : EarnWidgetState()
    data class Error(
        @StringRes val messageTextRes: Int,
        @StringRes val buttonTextRes: Int
    ) : EarnWidgetState()
}

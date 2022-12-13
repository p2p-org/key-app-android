package org.p2p.uikit.components

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.core.glide.GlideManager
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.core.token.Token
import org.p2p.core.utils.orZero
import org.p2p.uikit.databinding.WidgetSendDetailsInputBinding
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.withTextOrGone

class UiKitSendDetailsWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), KoinComponent {

    private val binding: WidgetSendDetailsInputBinding = inflateViewBinding()

    private val glideManager: GlideManager by inject()

    var amountListener: ((input: String) -> Unit)? = null
    var switchListener: (() -> Unit)? = null
    var tokenClickListener: (() -> Unit)? = null
    var maxButtonClickListener: (() -> Unit)? = null
    var feeButtonClickListener: (() -> Unit)? = null

    init {
        with(binding) {
            containerToken.setOnClickListener {
                tokenClickListener?.invoke()
            }
            viewSwitchToClickArea.setOnClickListener {
                switchListener?.invoke()
            }
            textViewFee.setOnClickListener {
                feeButtonClickListener?.invoke()
            }
            imageViewFeesInfo.setOnClickListener {
                feeButtonClickListener?.invoke()
            }
            textViewMax.setOnClickListener {
                maxButtonClickListener?.invoke()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        installAmountWatcher()
    }

    override fun onDetachedFromWindow() {
        AmountFractionTextWatcher.uninstallFrom(binding.editTextAmount)
        super.onDetachedFromWindow()
    }

    fun setToken(token: Token.Active) {
        with(binding) {
            glideManager.load(imageViewTokenIcon, token.iconUrl)
            textViewTokenName.text = token.tokenName
            textViewTokenTotal.text = token.getFormattedTotal(includeSymbol = true)
            textViewTokenAmountInUsd.text = token.getFormattedUsdTotal()
        }
    }

    fun setSwitchLabel(text: String) {
        binding.textViewAmountTypeSwitchLabel.text = text
    }

    fun setMainAmountLabel(text: String) {
        binding.textViewMainAmount.text = text
    }

    fun setFeeLabel(text: String?) {
        binding.textViewFee withTextOrGone text
    }

    fun showFeeLoading(isLoading: Boolean) {
        binding.progressBarFees.isVisible = isLoading
        binding.textViewFee.isEnabled = !isLoading
        binding.imageViewFeesInfo.isVisible = !isLoading
    }

    fun setAroundValue(aroundValue: String) {
        binding.textViewSecondAmount.text = aroundValue
    }

    fun setMaxButtonVisible(isVisible: Boolean) {
        binding.textViewMax.isVisible = isVisible
    }

    fun setInput(
        textValue: String,
        forced: Boolean,
        maxSymbolsAllowed: Int
    ) {
        with(binding.editTextAmount) {
            if (forced) {
                AmountFractionTextWatcher.uninstallFrom(this)
                setText(textValue)
                setSelection(text?.length.orZero())
                installAmountWatcher(maxSymbolsAllowed)
            } else {
                setText(textValue)
                setSelection(text?.length.orZero())
            }
        }
    }

    fun setInputTextColor(@ColorRes colorRes: Int) {
        binding.editTextAmount.setTextColor(getColor(colorRes))
    }

    fun focusAndShowKeyboard() {
        binding.editTextAmount.focusAndShowKeyboard()
    }

    private fun installAmountWatcher(maxSymbolsAllowed: Int = AmountFractionTextWatcher.MAX_FRACTION_LENGTH) {
        AmountFractionTextWatcher.installOn(binding.editTextAmount, maxSymbolsAllowed) {
            amountListener?.invoke(it)
        }
    }
}

interface UiKitSendDetailsWidgetContract {
    fun showToken(token: Token.Active)
    fun showAroundValue(value: String)
    fun setSwitchLabel(symbol: String)

    fun setInputColor(@ColorRes colorRes: Int)
    fun setMainAmountLabel(symbol: String)
    fun setMaxButtonVisible(isVisible: Boolean)

    fun showFeeViewLoading(isLoading: Boolean)
    fun setFeeLabel(text: String?)
}

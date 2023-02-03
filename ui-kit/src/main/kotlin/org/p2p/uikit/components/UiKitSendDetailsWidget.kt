package org.p2p.uikit.components

import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.postDelayed
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
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
import java.util.concurrent.atomic.AtomicInteger

private const val MAX_FRACTION_LENGTH = 9
private const val PROGRESS_DELAY_IN_MS = 200L

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

    private var maxFractionLength: AtomicInteger = AtomicInteger(MAX_FRACTION_LENGTH)

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
        installAmountWatcher(maxFractionLength.get())
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

    fun setTokenContainerEnabled(isEnabled: Boolean) {
        binding.containerToken.isEnabled = isEnabled
        binding.imageViewSelectToken.isVisible = isEnabled
    }

    fun setSwitchLabel(text: String) {
        binding.textViewAmountTypeSwitchLabel.text = text
    }

    fun setMainAmountLabel(text: String) {
        binding.textViewMainAmount.text = text
    }

    fun setFeeLabel(text: String) {
        binding.textViewFee.text = text
    }

    fun showFeeVisible(isVisible: Boolean) {
        binding.layoutFeeInfo.isVisible = isVisible
    }

    fun showFeeLoading(isLoading: Boolean) {
        binding.progressBarFees.isVisible = isLoading
        binding.imageViewFeesInfo.isVisible = !isLoading
    }

    fun showDelayedFeeViewLoading(isLoading: Boolean) {
        if (!isLoading) {
            handler.removeCallbacksAndMessages(null)
            showFeeLoading(isLoading = false)
            return
        }

        handler.postDelayed(PROGRESS_DELAY_IN_MS) {
            binding.progressBarFees.isVisible = true
            binding.imageViewFeesInfo.isVisible = false
        }
    }

    fun setAroundValue(aroundValue: String) {
        binding.textViewSecondAmount.text = aroundValue
    }

    fun setInputEnabled(isEnabled: Boolean) {
        binding.editTextAmount.isEnabled = isEnabled
    }

    fun setMaxButtonVisible(isVisible: Boolean) {
        binding.textViewMax.isVisible = isVisible
    }

    fun setInput(textValue: String, forced: Boolean) {
        with(binding.editTextAmount) {
            if (forced) {
                AmountFractionTextWatcher.uninstallFrom(this)
                setText(textValue)
                setSelection(textValue.length)
                installAmountWatcher(maxFractionLength.get())
            } else {
                setText(textValue)
                setSelection(text?.length.orZero())
            }
        }
    }

    fun disableInputs() {
        binding.imageViewSwitchTo.isVisible = false
        binding.viewSwitchToClickArea.isInvisible = true
        binding.textViewMax.isVisible = false
        binding.editTextAmount.isEnabled = false
        binding.textViewAmountTypeSwitchLabel.isVisible = false
    }

    fun setInputTextColor(@ColorRes colorRes: Int) {
        binding.editTextAmount.setTextColor(getColor(colorRes))
    }

    fun focusAndShowKeyboard() {
        binding.editTextAmount.focusAndShowKeyboard()
    }

    fun updateFractionLength(newFractionLength: Int) {
        maxFractionLength.set(newFractionLength)
        AmountFractionTextWatcher.uninstallFrom(binding.editTextAmount)
        installAmountWatcher(newFractionLength)
    }

    private fun installAmountWatcher(maxFractionLength: Int) {
        AmountFractionTextWatcher.installOn(
            editText = binding.editTextAmount,
            maxDecimalsAllowed = maxFractionLength,
            maxIntLength = Int.MAX_VALUE
        ) { amount ->
            amountListener?.invoke(amount)
        }
    }
}

interface UiKitSendDetailsWidgetContract {
    fun showToken(token: Token.Active)
    fun showAroundValue(value: String)
    fun showSliderCompleteAnimation()
    fun showFeeViewLoading(isLoading: Boolean)
    fun showDelayedFeeViewLoading(isLoading: Boolean)

    fun setSwitchLabel(symbol: String)
    fun setInputColor(@ColorRes colorRes: Int)
    fun setMainAmountLabel(symbol: String)
    fun setMaxButtonVisible(isVisible: Boolean)
    fun setFeeLabel(text: String)
    fun setTokenContainerEnabled(isEnabled: Boolean)
    fun setInputEnabled(isEnabled: Boolean)
    fun showFeeViewVisible(isVisible: Boolean)

    fun restoreSlider()
}

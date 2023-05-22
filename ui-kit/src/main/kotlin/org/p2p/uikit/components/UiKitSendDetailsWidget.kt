package org.p2p.uikit.components

import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.postDelayed
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicInteger
import org.p2p.core.glide.GlideManager
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.core.token.Token
import org.p2p.core.utils.orZero
import org.p2p.uikit.databinding.WidgetSendDetailsInputBinding
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bind

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

    private var isBottomFeeMode = false

    private var maxFractionLength: AtomicInteger = AtomicInteger(MAX_FRACTION_LENGTH)

    init {
        with(binding) {
            containerToken.setOnClickListener {
                tokenClickListener?.invoke()
            }
            viewSwitchToClickArea.setOnClickListener {
                switchListener?.invoke()
            }
            val feeClickListener: (View) -> Unit = { feeButtonClickListener?.invoke() }
            textViewFee.setOnClickListener(feeClickListener)
            textViewBottomFee.setOnClickListener(feeClickListener)
            imageViewFeesInfo.setOnClickListener(feeClickListener)
            imageViewBottomFeesInfo.setOnClickListener(feeClickListener)
            textViewMax.setOnClickListener {
                maxButtonClickListener?.invoke()
            }
        }
    }

    private val feeInfo: View
        get() = if (isBottomFeeMode) {
            binding.layoutBottomFeeInfo
        } else {
            binding.layoutFeeInfo
        }

    private val progressBarFees: View
        get() = if (isBottomFeeMode) {
            binding.progressBarBottomFees
        } else {
            binding.progressBarFees
        }

    private val imageViewFeesInfo: View
        get() = if (isBottomFeeMode) {
            binding.imageViewBottomFeesInfo
        } else {
            binding.imageViewFeesInfo
        }

    private val textViewFee: TextView
        get() = if (isBottomFeeMode) {
            binding.textViewBottomFee
        } else {
            binding.textViewFee
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

    fun switchToBottomFee() {
        isBottomFeeMode = true
        with(binding) {
            groupTopFee.isVisible = false
            groupBottomFee.isVisible = true
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
        textViewFee.text = text
    }

    fun setTotalLabel(text: String) {
        binding.textViewBottomTotal.text = text
    }

    fun setTotalValue(text: String) {
        binding.textViewBottomTotalValue.text = text
    }

    fun showFeeVisible(isVisible: Boolean) {
        feeInfo.isVisible = isVisible
    }

    fun showFeeLoading(isLoading: Boolean) {
        progressBarFees.isVisible = isLoading
        imageViewFeesInfo.isVisible = !isLoading
    }

    fun showBottomFeeValue(fee: TextViewCellModel) {
        binding.textViewBottomFeeValue.bind(fee)
    }

    fun setBottomFeeColor(@ColorRes colorRes: Int) = with(binding) {
        textViewBottomFee.setTextColor(getColor(colorRes))
        textViewBottomFeeValue.setTextColor(getColor(colorRes))
        imageViewBottomFeesInfo.setColorFilter(getColor(colorRes))
    }

    fun showDelayedFeeViewLoading(isLoading: Boolean) {
        if (!isLoading) {
            handler.removeCallbacksAndMessages(null)
            showFeeLoading(isLoading = false)
            return
        }

        handler.postDelayed(PROGRESS_DELAY_IN_MS) {
            progressBarFees.isVisible = true
            imageViewFeesInfo.isVisible = false
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

    fun enableFiat() {
        enableSwitchAmounts()
        binding.textViewSecondAmount.isVisible = true
    }

    fun disableFiat() {
        disableSwitchAmounts()
        binding.textViewSecondAmount.isVisible = false
    }

    fun disableInputs() {
        disableSwitchAmounts()
        binding.textViewMax.isVisible = false
        binding.editTextAmount.isEnabled = false
    }

    private fun enableSwitchAmounts() {
        binding.imageViewSwitchTo.isVisible = true
        binding.viewSwitchToClickArea.isVisible = true
        binding.textViewAmountTypeSwitchLabel.isVisible = true
    }

    private fun disableSwitchAmounts() {
        binding.imageViewSwitchTo.isVisible = false
        binding.viewSwitchToClickArea.isInvisible = true
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
    fun showBottomFeeValue(fee: TextViewCellModel)
    fun setFeeColor(@ColorRes colorRes: Int)
    fun setTotalValue(text: String)
    fun setTokenContainerEnabled(isEnabled: Boolean)
    fun setInputEnabled(isEnabled: Boolean)
    fun showFeeViewVisible(isVisible: Boolean)

    fun restoreSlider()
}

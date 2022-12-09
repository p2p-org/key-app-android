package org.p2p.uikit.components

import android.content.Context
import android.util.AttributeSet
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
import org.p2p.uikit.utils.inflateViewBinding
import java.math.BigDecimal

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
            imageViewSwitchTo.setOnClickListener {
                switchListener?.invoke()
            }
            textViewFreeTransactions.setOnClickListener {
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
            textViewTokenName.text = token.tokenSymbol
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

    fun setFeeLabel(text: String) {
        binding.textViewFreeTransactions.text = text
    }

    fun setFeeProgress(isVisible: Boolean) {
        binding.progressbarFreeTransactionsProgress.isVisible = isVisible
        binding.imageViewFreeTransactionsInfo.isVisible = !isVisible
    }

    fun setAroundValue(aroundValue: String) {
        binding.textViewSecondAmount.text = aroundValue
    }

    fun setMaxButtonVisibility(isVisible: Boolean) {
        binding.textViewMax.isVisible = isVisible
    }

    fun setInput(value: BigDecimal, forced: Boolean) {
        with(binding.editTextAmount) {
            val textValue = value.toPlainString()
            if (forced) {
                AmountFractionTextWatcher.uninstallFrom(this)
                setText(textValue)
                setSelection(textValue.length)
                installAmountWatcher()
            } else {
                setText(textValue)
                setSelection(text?.length.orZero())
            }
        }
    }

    private fun installAmountWatcher() {
        AmountFractionTextWatcher.installOn(binding.editTextAmount) {
            amountListener?.invoke(it)
        }
    }

    fun focusAndShowKeyboard() {
        binding.editTextAmount.focusAndShowKeyboard()
    }
}

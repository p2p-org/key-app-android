package org.p2p.uikit.components

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.p2p.uikit.databinding.WidgetSendDetailsInputBinding
import org.p2p.uikit.textwatcher.AmountFractionTextWatcher
import org.p2p.uikit.utils.inflateViewBinding

class UiKitSendDetailsWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: WidgetSendDetailsInputBinding = inflateViewBinding()

    var amountListener: ((input: String) -> Unit)? = null
    val switchListener: (() -> Unit)? = null
    val tokenClickListener: (() -> Unit)? = null

    val imageViewTokenIcon
        get() = binding.imageViewTokenIcon

    val textViewTokenName
        get() = binding.textViewTokenName

    val textViewTokenTotal
        get() = binding.textViewTokenTotal

    val textViewTokenAmountInUsd
        get() = binding.textViewTokenAmountInUsd

    init {
        with(binding) {
            containerToken.setOnClickListener {
                tokenClickListener?.invoke()
            }
            imageViewSwitchTo.setOnClickListener {
                switchListener?.invoke()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        AmountFractionTextWatcher.installOn(binding.editTextAmount) {
            amountListener?.invoke(it)
        }
    }

    override fun onDetachedFromWindow() {
        AmountFractionTextWatcher.uninstallFrom(binding.editTextAmount)
        super.onDetachedFromWindow()
    }

    fun setSwitchLabel(text: String) {
        binding.textViewAmountTypeSwitchLabel.text = text
    }

}

package org.p2p.uikit.components

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.core.glide.GlideManager
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.core.token.Token
import org.p2p.uikit.databinding.WidgetSendDetailsInputBinding
import org.p2p.uikit.utils.inflateViewBinding

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
}

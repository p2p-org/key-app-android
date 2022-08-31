package org.p2p.uikit.components

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetAmountsViewBinding
import org.p2p.uikit.utils.inflateViewBinding

class UiKitAmountsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = inflateViewBinding<WidgetAmountsViewBinding>()

    var token: String? = null
        set(value) {
            field = value
            binding.texViewToken.text = value
        }

    var currency: String? = null
        set(value) {
            field = value
            binding.texViewCurrency.text = value
        }

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_amounts_view)
    }

    private fun setOnTokenAmountChangeListener(onTokenAmountChange: (String) -> Unit) {
        binding.editTextTokenAmount.doAfterTextChanged { onTokenAmountChange(it.toString()) }
    }

    private fun setOnCurrencyAmountChangeListener(onCurrencyAmountChange: (String) -> Unit) {
        binding.editTextCurrencyAmount.doAfterTextChanged { onCurrencyAmountChange(it.toString()) }
    }

    private fun setOnSelectTokenClickListener(onSelectTokenClick: () -> Unit) {
        binding.imageViewSelectToken.setOnClickListener { onSelectTokenClick() }
    }

    private fun setOnSelectCurrencyClickListener(onSelectCurrencyClick: () -> Unit) {
        binding.imageViewSelectCurrency.setOnClickListener { onSelectCurrencyClick() }
    }
}

package com.p2p.wallet.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.p2p.wallet.R
import com.p2p.wallet.databinding.WidgetSlippageRadioViewBinding

class SlippageRadioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val MIN_SLIPPAGE = 0.1
        private const val MEDIUM_SLIPPAGE = 0.5
        private const val PERCENT_SLIPPAGE = 1.0
        private const val FIVE_PERCENT_SLIPPAGE = 5.0
    }

    var onSlippageChanged: ((Double) -> Unit)? = null

    private val binding = WidgetSlippageRadioViewBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        orientation = VERTICAL

        with(binding) {
            slippageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                onButtonChecked(checkedId)

                customRadioButton.isChecked = false
                customTextView.isVisible = false
                customEditText.isVisible = false
            }

            customEditText.doAfterTextChanged {
                val slippage = it.toString().toDoubleOrNull() ?: MIN_SLIPPAGE
                onSlippageChanged?.invoke(slippage)
            }
            customImageView.setOnClickListener {
                customRadioButton.isChecked = true
                slippageRadioGroup.clearCheck()
                customTextView.isVisible = true
                customEditText.isVisible = true
            }
        }
    }

    private fun onButtonChecked(checkedId: Int) {
        val slippage = when (checkedId) {
            R.id.minSlippageButton -> MIN_SLIPPAGE
            R.id.mediumSlippageButton -> MEDIUM_SLIPPAGE
            R.id.percentSlippageButton -> PERCENT_SLIPPAGE
            R.id.fivePercentSlippageButton -> FIVE_PERCENT_SLIPPAGE
            else -> MIN_SLIPPAGE
        }

        binding.customEditText.setText(slippage.toString())
    }
}
package com.p2p.wallet.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.p2p.wallet.R
import com.p2p.wallet.databinding.WidgetSlippageRadioViewBinding
import com.p2p.wallet.swap.model.Slippage

class SlippageRadioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

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
                val slippage = it.toString().toDoubleOrNull() ?: Slippage.MIN.doubleValue
                onSlippageChanged?.invoke(slippage)
            }

            customImageView.setOnClickListener {
                checkCustomButton()
            }
        }
    }

    private fun checkCustomButton() {
        binding.customRadioButton.isChecked = true
        binding.slippageRadioGroup.clearCheck()
        binding.customTextView.isVisible = true
        binding.customEditText.isVisible = true
    }

    fun setCurrentSlippage(slippage: Slippage) {
        when (slippage) {
            Slippage.MIN -> binding.slippageRadioGroup.check(R.id.minSlippageButton)
            Slippage.MEDIUM -> binding.slippageRadioGroup.check(R.id.mediumSlippageButton)
            Slippage.PERCENT -> binding.slippageRadioGroup.check(R.id.percentSlippageButton)
            Slippage.FIVE -> binding.slippageRadioGroup.check(R.id.fivePercentSlippageButton)
            Slippage.CUSTOM -> checkCustomButton()
        }
    }

    private fun onButtonChecked(checkedId: Int) {
        val slippage = when (checkedId) {
            R.id.minSlippageButton -> Slippage.MIN
            R.id.mediumSlippageButton -> Slippage.MEDIUM
            R.id.percentSlippageButton -> Slippage.PERCENT
            R.id.fivePercentSlippageButton -> Slippage.FIVE
            else -> Slippage.MIN
        }

        binding.customEditText.setText(slippage.doubleValue.toString())
    }
}
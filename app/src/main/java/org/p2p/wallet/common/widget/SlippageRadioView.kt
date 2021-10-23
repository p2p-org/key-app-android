package org.p2p.wallet.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetSlippageRadioViewBinding
import org.p2p.wallet.swap.model.Slippage

class SlippageRadioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onSlippageChanged: ((Slippage) -> Unit)? = null

    private val binding = WidgetSlippageRadioViewBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        orientation = VERTICAL

        with(binding) {
            customEditText.doAfterTextChanged {
                val slippage = it.toString().toDoubleOrNull()
                val result = slippage?.let { Slippage.parse(slippage) } ?: Slippage.MIN
                onSlippageChanged?.invoke(result)
            }
        }
    }

    fun setCurrentSlippage(slippage: Slippage) {
        with(binding) {
            customImageView.setOnClickListener {
                checkCustomButton(slippage.doubleValue)
            }

            slippageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                onButtonChecked(checkedId, slippage.doubleValue)

                customRadioButton.isChecked = false
                customTextView.isVisible = false
                customEditText.isVisible = false
            }

            when (slippage) {
                Slippage.MIN -> slippageRadioGroup.check(R.id.minSlippageButton)
                Slippage.MEDIUM -> slippageRadioGroup.check(R.id.mediumSlippageButton)
                Slippage.PERCENT -> slippageRadioGroup.check(R.id.percentSlippageButton)
                Slippage.FIVE -> slippageRadioGroup.check(R.id.fivePercentSlippageButton)
                is Slippage.CUSTOM -> checkCustomButton(slippage.doubleValue)
            }
        }
    }

    private fun checkCustomButton(customSlippage: Double) {
        binding.customRadioButton.isChecked = true
        binding.slippageRadioGroup.clearCheck()
        binding.customTextView.isVisible = true
        binding.customEditText.isVisible = true
        binding.customEditText.setText(customSlippage.toString())
    }

    private fun onButtonChecked(checkedId: Int, customSlippage: Double) {
        val slippage = when (checkedId) {
            R.id.minSlippageButton -> Slippage.MIN
            R.id.mediumSlippageButton -> Slippage.MEDIUM
            R.id.percentSlippageButton -> Slippage.PERCENT
            R.id.fivePercentSlippageButton -> Slippage.FIVE
            else -> Slippage.CUSTOM(customSlippage)
        }

        binding.customEditText.setText(slippage.doubleValue.toString())
    }
}
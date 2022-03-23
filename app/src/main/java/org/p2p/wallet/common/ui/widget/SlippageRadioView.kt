package org.p2p.wallet.common.ui.widget

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
                val result = slippage?.let { Slippage.parse(slippage) } ?: Slippage.Min
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
                Slippage.Min -> slippageRadioGroup.check(R.id.minSlippageButton)
                Slippage.Medium -> slippageRadioGroup.check(R.id.mediumSlippageButton)
                Slippage.Percent -> slippageRadioGroup.check(R.id.percentSlippageButton)
                Slippage.Five -> slippageRadioGroup.check(R.id.fivePercentSlippageButton)
                is Slippage.Custom -> checkCustomButton(slippage.doubleValue)
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
            R.id.minSlippageButton -> Slippage.Min
            R.id.mediumSlippageButton -> Slippage.Medium
            R.id.percentSlippageButton -> Slippage.Percent
            R.id.fivePercentSlippageButton -> Slippage.Five
            else -> Slippage.Custom(customSlippage)
        }

        binding.customEditText.setText(slippage.doubleValue.toString())
    }
}

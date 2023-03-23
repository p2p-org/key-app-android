package org.p2p.wallet.jupiter.ui.settings.view

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.core.utils.DecimalFormatter
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.getString
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSwapCustomSlippageBinding
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.Slippage.Companion.MAX_ALLOWED_SLIPPAGE
import org.p2p.wallet.swap.model.Slippage.Companion.PERCENT_DIVIDE_VALUE

fun swapCustomSlippageDelegate(
    onCustomSlippageChange: (value: Double?) -> Unit,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<SwapCustomSlippageCellModel, AnyCellItem, ItemSwapCustomSlippageBinding>(
        viewBinding = { _, parent -> parent.inflateViewBinding(attachToRoot = false) },
    ) {

        val nightText = binding.getColor(R.color.text_night)
        val roseText = binding.getColor(R.color.text_rose)
        var internalOnAmountChanged: ((value: String) -> Unit)? = null
        AmountFractionTextWatcher.installOn(
            editText = binding.textInputEditTextCustomSlippage,
            maxDecimalsAllowed = 2,
            maxIntLength = 3,
            onValueChanged = { internalOnAmountChanged?.invoke(it) }
        )

        fun validateSlippage(slippage: String) {
            val value = slippage.toDoubleOrNull() ?: 0.0
            when {
                value > MAX_ALLOWED_SLIPPAGE || value < Slippage.Min.doubleValue -> {
                    binding.textInputLayoutCustomSlippage.error =
                        binding.getString(R.string.swap_settings_custom_slippage_hint)
                    binding.textInputEditTextCustomSlippage.setTextColor(roseText)
                }
                else -> {
                    binding.textInputLayoutCustomSlippage.error = null
                    binding.textInputEditTextCustomSlippage.setTextColor(nightText)
                    onCustomSlippageChange(value)
                }
            }
        }

        bind {
            val slippage = (item.slippage.doubleValue * PERCENT_DIVIDE_VALUE)
            val text = DecimalFormatter.format(value = slippage, decimals = 2)
            with(binding) {
                if (text != textInputEditTextCustomSlippage.text?.toString()) {
                    internalOnAmountChanged = null
                    textInputEditTextCustomSlippage.setText(text)
                    validateSlippage(text)
                    textInputEditTextCustomSlippage.setSelection(text.length)
                    internalOnAmountChanged = ::validateSlippage
                }
            }
        }
    }

data class SwapCustomSlippageCellModel(
    val slippage: Slippage,
) : AnyCellItem

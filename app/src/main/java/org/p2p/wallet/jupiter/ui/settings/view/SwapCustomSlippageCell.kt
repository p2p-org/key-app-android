package org.p2p.wallet.jupiter.ui.settings.view

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.core.utils.DecimalFormatter
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.getString
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSwapCustomSlippageBinding
import org.p2p.wallet.swap.model.MAX_ALLOWED_SLIPPAGE
import org.p2p.wallet.swap.model.PERCENT_DIVIDE_VALUE
import org.p2p.wallet.swap.model.Slippage

fun swapCustomSlippageDelegate(
    onCustomSlippageChange: (value: Double?) -> Unit,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<SwapCustomSlippageCellModel, AnyCellItem, ItemSwapCustomSlippageBinding>(
        viewBinding = { inflater, parent -> ItemSwapCustomSlippageBinding.inflate(inflater, parent, false) },
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
            val text = DecimalFormatter.format(slippage, 2)
            with(binding) {
                if (text != textInputEditTextCustomSlippage.text?.toString()) {
                    internalOnAmountChanged = null
                    textInputEditTextCustomSlippage.setText(text)
                    validateSlippage(text)
                    textInputEditTextCustomSlippage.setSelection(text.length)
                    internalOnAmountChanged = { validateSlippage(it) }
                }
            }
        }
    }

data class SwapCustomSlippageCellModel(
    val slippage: Slippage,
) : AnyCellItem

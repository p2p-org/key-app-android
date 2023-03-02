package org.p2p.wallet.swap.ui.jupiter.settings.view

import androidx.core.widget.doAfterTextChanged
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSwapCustomSlippageBinding

fun swapCustomSlippageDelegate(
    onCustomSlippageChange: (value: Double?) -> Unit,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<SwapCustomSlippageCellModel, AnyCellItem, ItemSwapCustomSlippageBinding>(
        viewBinding = { inflater, parent -> ItemSwapCustomSlippageBinding.inflate(inflater, parent, false) },
    ) {

        val nightText = binding.getColor(R.color.text_night)
        val roseText = binding.getColor(R.color.text_rose)

        binding.editTextCustomSlippage.doAfterTextChanged { text ->
            val customSlippage = text?.toString()?.toDoubleOrNull()
            onCustomSlippageChange(customSlippage)
        }

        bind {
            binding.editTextCustomSlippage.setText(item.slippage?.toString())
            if (item.isValid) {
                binding.editTextCustomSlippage.setTextColor(nightText)
            } else {
                binding.editTextCustomSlippage.setTextColor(roseText)
            }
        }
    }

data class SwapCustomSlippageCellModel(
    val slippage: Double?,
    val isValid: Boolean
) : AnyCellItem

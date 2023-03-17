package org.p2p.uikit.components.finance_block

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.databinding.ItemFinanceBlockBinding
import org.p2p.uikit.model.AnyCellItem

fun financeBlockCellDelegate(
    inflateListener: ((financeBlock: UiKitFinanceBlockView) -> Unit)? = null,
    onBindListener: ((view: UiKitFinanceBlockView, item: FinanceBlockCellModel) -> Unit)? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<FinanceBlockCellModel, AnyCellItem, ItemFinanceBlockBinding>(
        viewBinding = { inflater, parent -> ItemFinanceBlockBinding.inflate(inflater, parent, false) },
        on = { item, _, _ -> item is FinanceBlockCellModel && item.styleType == FinanceBlockStyle.FINANCE_BLOCK }
    ) {

        binding.root.bindViewStyle(FinanceBlockStyle.FINANCE_BLOCK)
        inflateListener?.invoke(binding.root)

        bind {
            binding.root.bind(item)
            onBindListener?.invoke(binding.root, item)
        }
    }

fun baseCellDelegate(
    inflateListener: ((financeBlock: UiKitFinanceBlockView) -> Unit)? = null,
    onBindListener: ((view: UiKitFinanceBlockView, item: FinanceBlockCellModel) -> Unit)? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<FinanceBlockCellModel, AnyCellItem, ItemFinanceBlockBinding>(
        viewBinding = { inflater, parent -> ItemFinanceBlockBinding.inflate(inflater, parent, false) },
        on = { item, _, _ -> item is FinanceBlockCellModel && item.styleType == FinanceBlockStyle.BASE_CELL }
    ) {

        binding.root.bindViewStyle(FinanceBlockStyle.BASE_CELL)
        inflateListener?.invoke(binding.root)

        bind {
            binding.root.bind(item)
            onBindListener?.invoke(binding.root, item)
        }
    }

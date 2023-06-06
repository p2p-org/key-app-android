package org.p2p.uikit.components.finance_block

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.databinding.ItemFinanceBlockBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.inflateViewBinding

private typealias InflateListener = (financeBlock: UiKitFinanceBlockView) -> Unit
private typealias OnBindListener = (view: UiKitFinanceBlockView, item: FinanceBlockCellModel) -> Unit

fun financeBlockCellDelegate(
    inflateListener: ((financeBlock: UiKitFinanceBlockView) -> Unit)? = null,
    onBindListener: ((view: UiKitFinanceBlockView, item: FinanceBlockCellModel) -> Unit)? = null,
    onItemClicked: (item: FinanceBlockCellModel) -> Unit = {}
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<FinanceBlockCellModel, AnyCellItem, ItemFinanceBlockBinding>(
        viewBinding = { _, parent -> parent.inflateViewBinding(attachToRoot = false) },
        on = { item, _, _ -> item is FinanceBlockCellModel && item.styleType == FinanceBlockStyle.FINANCE_BLOCK }
    ) {

        binding.root.bindViewStyle(FinanceBlockStyle.FINANCE_BLOCK)
        inflateListener?.invoke(binding.root)

        bind {
            binding.root.bind(item)
            onBindListener?.invoke(binding.root, item)
            binding.root.setOnClickListener { onItemClicked.invoke(item) }
        }
    }

fun baseCellDelegate(
    inflateListener: InflateListener? = null,
    onBindListener: OnBindListener? = null,
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

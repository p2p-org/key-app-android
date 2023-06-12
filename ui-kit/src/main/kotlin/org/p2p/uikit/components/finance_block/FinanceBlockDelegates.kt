package org.p2p.uikit.components.finance_block

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.databinding.ItemMainCellBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.inflateViewBinding

private typealias InflateListener = (financeBlock: UiKitMainCellView) -> Unit
private typealias OnBindListener = (view: UiKitMainCellView, item: MainCellModel) -> Unit

fun mainCellDelegate(
    inflateListener: ((financeBlock: UiKitMainCellView) -> Unit)? = null,
    onBindListener: ((view: UiKitMainCellView, item: MainCellModel) -> Unit)? = null,
    onItemClicked: (item: MainCellModel) -> Unit = {}
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<MainCellModel, AnyCellItem, ItemMainCellBinding>(
        viewBinding = { _, parent -> parent.inflateViewBinding(attachToRoot = false) },
        on = { item, _, _ -> item is MainCellModel && item.styleType == MainCellStyle.FINANCE_BLOCK }
    ) {

        binding.root.bindViewStyle(MainCellStyle.FINANCE_BLOCK)
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
    adapterDelegateViewBinding<MainCellModel, AnyCellItem, ItemMainCellBinding>(
        viewBinding = { inflater, parent -> ItemMainCellBinding.inflate(inflater, parent, false) },
        on = { item, _, _ -> item is MainCellModel && item.styleType == MainCellStyle.BASE_CELL }
    ) {

        binding.root.bindViewStyle(MainCellStyle.BASE_CELL)
        inflateListener?.invoke(binding.root)

        bind {
            binding.root.bind(item)
            onBindListener?.invoke(binding.root, item)
        }
    }

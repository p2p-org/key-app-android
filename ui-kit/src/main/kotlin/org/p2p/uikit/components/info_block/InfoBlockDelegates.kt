package org.p2p.uikit.components.info_block

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.databinding.ItemInfoBlockBinding
import org.p2p.uikit.model.AnyCellItem

fun infoBlockCellDelegate(
    inflateListener: ((financeBlock: UiKitInfoBlockView) -> Unit)? = null,
    onBindListener: ((view: UiKitInfoBlockView, item: InfoBlockCellModel) -> Unit)? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<InfoBlockCellModel, AnyCellItem, ItemInfoBlockBinding>(
        viewBinding = { inflater, parent -> ItemInfoBlockBinding.inflate(inflater, parent, false) }
    ) {

        inflateListener?.invoke(binding.root)

        bind {
            binding.root.bind(item)
            onBindListener?.invoke(binding.root, item)
        }
    }

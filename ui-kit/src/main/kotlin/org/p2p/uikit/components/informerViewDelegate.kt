package org.p2p.uikit.components

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.databinding.ItemInformerViewBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.inflateViewBinding

fun informerViewDelegate(
    inflateListener: ((financeBlock: UiKitInformerView) -> Unit)? = null,
    onBindListener: ((view: UiKitInformerView, item: InformerViewCellModel) -> Unit)? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<InformerViewCellModel, AnyCellItem, ItemInformerViewBinding>(
        viewBinding = { _, parent -> parent.inflateViewBinding(attachToRoot = false) },
    ) {
        bind {
            binding.root.bind(item)
        }
    }

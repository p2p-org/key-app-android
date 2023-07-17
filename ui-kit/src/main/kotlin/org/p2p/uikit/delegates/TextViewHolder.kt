package org.p2p.uikit.delegates

import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.databinding.ItemTextBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.uikit.utils.text.bind

fun textViewCellDelegate(): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<TextViewCellModel, AnyCellItem, ItemTextBinding>(
        viewBinding = { inflater, parent -> ItemTextBinding.inflate(inflater, parent, false) },
    ) {

        bind {
            binding.root.bind(item)
        }
    }

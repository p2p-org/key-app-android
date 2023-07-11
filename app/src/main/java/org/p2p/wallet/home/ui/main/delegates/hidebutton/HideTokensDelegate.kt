package org.p2p.wallet.home.ui.main.delegates.hidebutton

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.databinding.ItemTokenGroupButtonBinding

private typealias BindListener = ((binding: ItemTokenGroupButtonBinding, item: TokenButtonCellModel) -> Unit)

private val inflateViewBinding = { inflater: LayoutInflater, parent: ViewGroup ->
    ItemTokenGroupButtonBinding.inflate(inflater, parent, false)
}

fun tokenButtonDelegate(
    onBindListener: BindListener? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<TokenButtonCellModel, AnyCellItem, ItemTokenGroupButtonBinding>(
        viewBinding = inflateViewBinding
    ) {
        bind {
            binding.imageViewTokenState.setImageResource(item.visibilityIcon)
            onBindListener?.invoke(binding, item)
        }
    }

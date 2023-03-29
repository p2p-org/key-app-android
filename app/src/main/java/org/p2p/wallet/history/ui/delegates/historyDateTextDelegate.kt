package org.p2p.wallet.history.ui.delegates

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.databinding.ItemHistoryDateBinding

private typealias InflateListener = ((financeBlock: TextView) -> Unit)
private typealias BindListener = ((view: TextView, item: HistoryDateCellModel) -> Unit)

private val inflateViewBinding = { inflater: LayoutInflater, parent: ViewGroup ->
    ItemHistoryDateBinding.inflate(inflater, parent, false)
}

fun historyDateTextDelegate(
    inflateListener: InflateListener? = null,
    onBindListener: BindListener? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<HistoryDateCellModel, AnyCellItem, ItemHistoryDateBinding>(
        viewBinding = inflateViewBinding
    ) {
        inflateListener?.invoke(binding.root)

        bind {
            binding.root.text = item.getFormattedDate(context)
            onBindListener?.invoke(binding.root, item)
        }
    }

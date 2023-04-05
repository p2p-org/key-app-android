package org.p2p.wallet.common.adapter

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import java.util.Objects
import org.p2p.uikit.model.AnyCellItem

class CommonAnyCellAdapter(
    vararg delegates: AdapterDelegate<List<AnyCellItem>>,
    diffUtilCallback: DiffUtil.ItemCallback<AnyCellItem> = DefaultDiffCallback()
) : AsyncListDifferDelegationAdapter<AnyCellItem>(diffUtilCallback, *delegates)

private class DefaultDiffCallback : DiffUtil.ItemCallback<AnyCellItem>() {

    override fun areItemsTheSame(oldItem: AnyCellItem, newItem: AnyCellItem): Boolean {
        return when {
            else -> oldItem::class == newItem::class
        }
    }

    override fun areContentsTheSame(oldItem: AnyCellItem, newItem: AnyCellItem): Boolean {

        return Objects.equals(oldItem, newItem)
    }
}

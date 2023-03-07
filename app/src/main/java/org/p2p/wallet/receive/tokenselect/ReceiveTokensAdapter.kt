package org.p2p.wallet.receive.tokenselect

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import java.util.Objects
import org.p2p.uikit.model.AnyCellItem

class ReceiveTokensAdapter(
    vararg delegates: AdapterDelegate<List<AnyCellItem>>
) : AsyncListDifferDelegationAdapter<AnyCellItem>(DiffCallback(), *delegates)

private class DiffCallback : DiffUtil.ItemCallback<AnyCellItem>() {

    override fun areItemsTheSame(oldItem: AnyCellItem, newItem: AnyCellItem): Boolean {
        return when {
            else -> oldItem::class == newItem::class
        }
    }

    override fun areContentsTheSame(oldItem: AnyCellItem, newItem: AnyCellItem): Boolean {
        return Objects.equals(oldItem, newItem)
    }
}

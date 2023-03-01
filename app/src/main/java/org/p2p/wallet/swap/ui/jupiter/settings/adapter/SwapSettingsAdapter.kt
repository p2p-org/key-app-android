package org.p2p.wallet.swap.ui.jupiter.settings.adapter

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import java.util.Objects
import org.p2p.uikit.model.AnyCellItem

class SwapSettingsAdapter(
    vararg delegates: AdapterDelegate<List<AnyCellItem>>
) : AsyncListDifferDelegationAdapter<AnyCellItem>(DiffCallback(), *delegates)

private class DiffCallback : DiffUtil.ItemCallback<AnyCellItem>() {

    override fun areItemsTheSame(oldItem: AnyCellItem, newItem: AnyCellItem): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }

    override fun areContentsTheSame(oldItem: AnyCellItem, newItem: AnyCellItem): Boolean {
        return Objects.equals(oldItem, newItem)
    }
}

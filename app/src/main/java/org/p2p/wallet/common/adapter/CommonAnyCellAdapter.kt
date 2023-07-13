package org.p2p.wallet.common.adapter

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import java.util.Objects
import org.p2p.uikit.model.AnyCellItem

class CommonAnyCellAdapter(
    vararg delegates: AdapterDelegate<List<AnyCellItem>>,
    diffUtilCallback: DiffUtil.ItemCallback<AnyCellItem> = DefaultDiffCallback()
) : AsyncListDifferDelegationAdapter<AnyCellItem>(diffUtilCallback, *delegates) {

    @Suppress("UNCHECKED_CAST")
    fun <T : AnyCellItem> updateItem(
        predicate: (AnyCellItem) -> Boolean,
        transform: (T) -> T,
        animateChanges: Boolean = false
    ) {
        // this.items is an ImmutableList so we can't just set the single element
        val oldItems = items.toMutableList()
        val index = oldItems.indexOfFirst(predicate)
        if (index != -1) {
            val old = oldItems[index] as T
            val new = transform(old)
            items = oldItems.apply { set(index, new) }
            if (animateChanges) {
                notifyItemChanged(index)
            } else {
                // workaround for disabling animation
                notifyItemChanged(index, Unit)
            }
        }
    }
}

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

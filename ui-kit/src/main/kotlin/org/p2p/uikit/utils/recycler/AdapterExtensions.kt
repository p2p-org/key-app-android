package org.p2p.uikit.utils.recycler

import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import org.p2p.uikit.model.AnyCellItem
@Suppress("UNCHECKED_CAST")
fun RecyclerView.Adapter<*>.getItems(): List<AnyCellItem> {
    return when (this) {
        is AsyncListDifferDelegationAdapter<*> -> items as List<AnyCellItem>
        is ListDelegationAdapter<*> -> items as List<AnyCellItem>
        is CustomBaseAdapter -> getItems()
        else -> emptyList()
    }
}

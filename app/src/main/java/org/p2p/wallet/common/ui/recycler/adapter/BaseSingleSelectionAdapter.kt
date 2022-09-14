package org.p2p.wallet.common.ui.recycler.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseSingleSelectionAdapter<T, VH : BaseSelectionViewHolder<T>>(
    preselectedItem: T? = null,
    private val onItemClicked: (T) -> Unit = {}
) : RecyclerView.Adapter<VH>() {

    private val data = mutableListOf<T>()

    var selectedItem: T? = preselectedItem

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH = onCreateViewHolder(parent, viewType) { item ->
        if (selectedItem != item) {
            selectedItem = item
            notifyDataSetChanged()
        }
        onItemClicked(item)
    }

    abstract fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
        onItemClicked: (T) -> Unit,
    ): VH

    override fun onBindViewHolder(
        holder: VH,
        position: Int
    ) {
        onBindViewHolder(holder, getItem(position), selectedItem)
    }

    abstract fun onBindViewHolder(
        holder: VH,
        item: T,
        selectedItem: T?
    )

    fun setItems(new: List<T>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    private fun getItem(position: Int): T = data[position]
}

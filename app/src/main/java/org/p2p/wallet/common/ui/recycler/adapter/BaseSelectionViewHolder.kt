package org.p2p.wallet.common.ui.recycler.adapter

import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView

abstract class BaseSelectionViewHolder<in T>(
    val root: View,
    private val onItemClicked: (item: T) -> Unit,
) : RecyclerView.ViewHolder(root) {

    @CallSuper
    open fun onBind(item: T, selectedItem: T?) {
        root.setOnClickListener {
            onItemClicked(item)
        }
    }
}

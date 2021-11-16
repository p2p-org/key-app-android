package org.p2p.wallet.history.ui.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemHistoryEmptyBinding

class EmptyViewHolder(
    binding: ItemHistoryEmptyBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(
        ItemHistoryEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
}
package org.p2p.wallet.home.ui.main.adapter

import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemTitleBinding
import org.p2p.wallet.home.model.HomeElementItem

class TitleViewHolder(
    private val binding: ItemTitleBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: HomeElementItem.Title) {
        binding.textViewTitle.setText(item.titleResId)
    }
}

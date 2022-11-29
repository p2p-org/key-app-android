package org.p2p.wallet.home.ui.new.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemPickTokenCategoryBinding
import org.p2p.wallet.home.model.SelectTokenItem

class CategoryTitleViewHolder(
    private val binding: ItemPickTokenCategoryBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(
        binding = ItemPickTokenCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    fun onBind(item: SelectTokenItem.CategoryTitle) {
        binding.root.setText(item.titleRes)
    }
}

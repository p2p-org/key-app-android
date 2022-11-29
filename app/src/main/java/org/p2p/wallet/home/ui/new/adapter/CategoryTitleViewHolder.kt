package org.p2p.wallet.home.ui.new.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemPickTokenCategoryBinding
import org.p2p.wallet.home.model.SelectTokenItem
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class CategoryTitleViewHolder(
    parent: ViewGroup,
    private val binding: ItemPickTokenCategoryBinding = parent.inflateViewBinding(attachToRoot = false)
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: SelectTokenItem.CategoryTitle) {
        binding.root.setText(item.titleRes)
    }
}

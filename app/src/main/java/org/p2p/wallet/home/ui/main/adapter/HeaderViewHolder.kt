package org.p2p.wallet.home.ui.main.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemMainHeaderBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class HeaderViewHolder(
    parent: ViewGroup,
    private val binding: ItemMainHeaderBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(text: String) = with(binding) {
        textViewHeader.text = text
    }
}

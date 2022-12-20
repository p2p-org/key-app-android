package org.p2p.wallet.send.ui.search.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.databinding.ItemSearchEmptyBalanceBinding
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.CUT_SEVEN_SYMBOLS
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SearchEmptyBalanceViewHolder(
    parent: ViewGroup,
    private val onItemClicked: (SearchResult) -> Unit,
    private val binding: ItemSearchEmptyBalanceBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: SearchResult.EmptyBalance) {
        with(binding) {
            textViewAddress.text = item.addressState.address.cutMiddle(CUT_SEVEN_SYMBOLS)
        }

        itemView.setOnClickListener { onItemClicked(item) }
    }
}

package org.p2p.wallet.history.ui.token.adapter.holders

import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemUsdUsdtSwapBannerBinding
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class HistorySwapBannerViewHolder(
    parent: ViewGroup,
    private val onHistoryClicked: (HistoryItem) -> Unit,
    private val binding: ItemUsdUsdtSwapBannerBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(item: HistoryItem.SwapBannerItem) {
        binding.buttonAction.setOnClickListener { onHistoryClicked(item) }
    }
}

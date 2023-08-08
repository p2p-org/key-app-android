package org.p2p.wallet.history.ui.token.adapter.holders

import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemUsdUsdtSwapBannerBinding
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class HistorySwapBannerViewHolder(
    parent: ViewGroup,
    private val onHistoryClicked: (HistoryItem) -> Unit,
    private val binding: ItemUsdUsdtSwapBannerBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(item: HistoryItem.SwapBannerItem) = with(binding) {
        textViewTitle.text = getString(
            R.string.bridge_usdc_usdt_swap_message,
            item.sourceTokenSymbol,
            item.destinationTokenSymbol
        )
        buttonAction.apply {
            text = getString(
                R.string.bridge_usdc_usdt_swap_action,
                item.sourceTokenSymbol,
                item.destinationTokenSymbol
            )
            setOnClickListener { onHistoryClicked(item) }
        }
    }
}

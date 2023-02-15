package org.p2p.wallet.history.ui.token.adapter.holders

import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.databinding.ItemTransactionSwapBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class TransactionSwapViewHolder(
    parent: ViewGroup,
    private val glideManager: GlideManager,
    private val onHistoryClicked: (HistoryItem) -> Unit,
    private val binding: ItemTransactionSwapBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(item: HistoryItem.TransactionItem) {
        itemView.setOnClickListener { onHistoryClicked(item) }
        with(binding) {
            transactionTokenImageView.setSourceAndDestinationImages(
                glideManager,
                item.sourceIconUrl,
                item.destinationIconUrl
            )
            with(transactionData) {
                startAmountView.title = item.startTitle
                startAmountView.subtitle = item.startSubtitle
                endAmountView.topValue = item.endTopValue
                item.endTopValueTextColor?.let { endAmountView.setTopValueTextColor(getColor(it)) }
                endAmountView.bottomValue = item.endBottomValue
            }
            transactionData.startAmountView.setSubtitleDrawable(left = item.statusIcon ?: 0)
        }
    }
}

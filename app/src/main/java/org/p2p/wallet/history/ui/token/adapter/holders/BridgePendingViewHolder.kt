package org.p2p.wallet.history.ui.token.adapter.holders

import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.databinding.ItemHistoryTransactionBinding
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class BridgePendingViewHolder(
    parent: ViewGroup,
    private val glideManager: GlideManager,
    private val onHistoryClicked: (HistoryItem) -> Unit,
    private val binding: ItemHistoryTransactionBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(item: HistoryItem.BridgeSendItem) {
        itemView.setOnClickListener { onHistoryClicked(item) }
        with(binding) {
            transactionTokenImageView.apply {
                ERC20Tokens.ETH.tokenIconUrl.also { setTokenImage(glideManager, it) }
            }
//            startAmountView.title = context.getString(R.string.bridge_ethereum_network)
//            startAmountView.subtitle = context.getString(R.string.bridge_send_pending)
//            endAmountView.topValue = item.sendDetails.amount
//            item.endTopValueTextColor?.let { endAmountView.setTopValueTextColor(getColor(it)) }
//            endAmountView.bottomValue = item.endBottomValue
//            startAmountView.setSubtitleDrawable(left = item.statusIcon ?: 0)
        }
    }

    fun onBind(item: HistoryItem.BridgeClaimItem) {
        itemView.setOnClickListener { onHistoryClicked(item) }
        with(binding) {
//            transactionTokenImageView.apply {
//                item.tokenIconUrl
//                    ?.also { setTokenImage(glideManager, it) }
//                    ?: setTransactionIcon(item.iconRes)
//            }
//            startAmountView.title = item.startTitle
//            startAmountView.subtitle = item.startSubtitle
//            endAmountView.topValue = item.endTopValue
//            item.endTopValueTextColor?.let { endAmountView.setTopValueTextColor(getColor(it)) }
//            endAmountView.bottomValue = item.endBottomValue
//            startAmountView.setSubtitleDrawable(left = item.statusIcon ?: 0)
        }
    }
}

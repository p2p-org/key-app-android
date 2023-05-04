package org.p2p.wallet.history.ui.token.adapter.holders

import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemHistoryTransactionBinding
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.history.ui.model.HistoryItem

class BridgePendingViewHolder(
    parent: ViewGroup,
    private val glideManager: GlideManager,
    private val onHistoryClicked: (HistoryItem) -> Unit,
    private val binding: ItemHistoryTransactionBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(item: HistoryItem.BridgeSendItem) {
        itemView.setOnClickListener { onHistoryClicked(item) }
        with(binding) {
            //TODO fix this
            transactionTokenImageView.apply {
                ERC20Tokens.values().firstOrNull { it.mintAddress == item.sendDetails.recipient.raw }
                    ?.let { setTokenImage(glideManager, it.tokenIconUrl) }
            }
            startAmountView.title = context.getString(R.string.bridge_ethereum_network)
            startAmountView.subtitle = context.getString(R.string.bridge_send_pending)
            endAmountView.topValue = item.sendDetails.amount.toString()
            endAmountView.setTopValueTextColor(context.getColor(R.color.text_night))
            endAmountView.bottomValue = item.sendDetails.amount.toString()
            startAmountView.setSubtitleDrawable(left = R.drawable.ic_state_pending)
            transactionTokenImageView.apply {
                ERC20Tokens.ETH.tokenIconUrl.also { setTokenImage(glideManager, it) }
            }
        }
    }

    fun onBind(item: HistoryItem.BridgeClaimItem) {
        itemView.setOnClickListener { onHistoryClicked(item) }
        with(binding) {
            //TODO fix this
            transactionTokenImageView.apply {
                ERC20Tokens.values().firstOrNull { it.mintAddress == item.bundle.recipient.raw }
                    ?.let { setTokenImage(glideManager, it.tokenIconUrl) }
            }
            startAmountView.title = context.getString(R.string.bridge_ethereum_network)
            startAmountView.subtitle = context.getString(R.string.bridge_send_pending)
            endAmountView.topValue = item.bundle.resultAmount.amountInUsd
            endAmountView.setTopValueTextColor(context.getColor(R.color.text_night))
            endAmountView.bottomValue = item.bundle.resultAmount.amountInToken.toString()
            startAmountView.setSubtitleDrawable(left = R.drawable.ic_state_pending)
        }
    }
}

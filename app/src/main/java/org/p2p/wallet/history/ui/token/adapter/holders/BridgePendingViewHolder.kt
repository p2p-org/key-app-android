package org.p2p.wallet.history.ui.token.adapter.holders

import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.ethereumkit.external.model.ERC20Tokens
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemHistoryTransactionBinding
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.utils.viewbinding.context
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
            val foundToken = ERC20Tokens.values()
                .firstOrNull { it.replaceTokenSymbol == item.sendDetails.amount.symbol }
            val iconUrl = item.tokenIconUrl ?: foundToken?.tokenIconUrl
            if (iconUrl != null) {
                transactionTokenImageView.setTokenImage(glideManager, iconUrl)
            } else {
                transactionTokenImageView.setTransactionIcon(R.drawable.ic_transaction_unknown)
            }
            startAmountView.title = context.getString(R.string.bridge_to_ethereum)
            startAmountView.subtitle = context.getString(R.string.bridge_send_pending)
            endAmountView.topValue = item.getFormattedFiatValue()
            endAmountView.setTopValueTextColor(context.getColor(R.color.text_night))
            endAmountView.bottomValue = item.getFormattedTotal()
            startAmountView.setSubtitleDrawable(left = R.drawable.ic_state_pending)
        }
    }

    fun onBind(item: HistoryItem.BridgeClaimItem) {
        itemView.setOnClickListener { onHistoryClicked(item) }
        with(binding) {
            val foundToken = ERC20Tokens.values()
                .firstOrNull { it.replaceTokenSymbol == item.bundle.resultAmount.symbol }
            val iconUrl = item.tokenIconUrl ?: foundToken?.tokenIconUrl
            if (iconUrl != null) {
                transactionTokenImageView.setTokenImage(glideManager, iconUrl)
            } else {
                transactionTokenImageView.setTransactionIcon(R.drawable.ic_transaction_unknown)
            }
            startAmountView.title = context.getString(R.string.bridge_from_ethereum)
            startAmountView.subtitle = context.getString(R.string.bridge_claim_pending)
            endAmountView.topValue = item.getFormattedFiatValue()
            endAmountView.setTopValueTextColor(context.getColor(R.color.text_night))
            endAmountView.bottomValue = item.getFormattedTotal()
            startAmountView.setSubtitleDrawable(left = R.drawable.ic_state_pending)
        }
    }
}

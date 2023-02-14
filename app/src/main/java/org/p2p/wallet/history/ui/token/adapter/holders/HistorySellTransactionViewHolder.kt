package org.p2p.wallet.history.ui.token.adapter.holders

import android.content.res.ColorStateList
import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemHistoryMoonpayTransactionBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class HistorySellTransactionViewHolder(
    parent: ViewGroup,
    private val onHistoryClicked: (HistoryItem) -> Unit,
    private val binding: ItemHistoryMoonpayTransactionBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {
    fun onBind(item: HistoryItem.MoonpayTransactionItem) = with(binding) {
        root.setOnClickListener { onHistoryClicked.invoke(item) }
        renderStatusIcon(item)
        renderTitleAndSubtitle(item)
        renderAmounts(item)
    }

    private fun ItemHistoryMoonpayTransactionBinding.renderStatusIcon(item: HistoryItem.MoonpayTransactionItem) {
        imageViewTransactionStatusIcon.setImageResource(item.statusIconRes)
        imageViewTransactionStatusIcon.setBackgroundResource(item.statusBackgroundRes)
        imageViewTransactionStatusIcon.imageTintList = ColorStateList.valueOf(getColor(item.statusIconColor))
    }

    private fun ItemHistoryMoonpayTransactionBinding.renderTitleAndSubtitle(item: HistoryItem.MoonpayTransactionItem) {
        layoutTransactionDetails.startAmountView.title = item.titleStatus
        layoutTransactionDetails.startAmountView.subtitle = item.subtitleReceiver
    }

    private fun renderAmounts(item: HistoryItem.MoonpayTransactionItem) {
        binding.layoutTransactionDetails.endAmountView.topValue = item.endTopValue
        binding.layoutTransactionDetails.endAmountView.bottomValue = null // hide SOL amount
    }
}

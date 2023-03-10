package org.p2p.wallet.history.ui.token.adapter.holders

import android.content.res.ColorStateList
import android.view.ViewGroup
import org.p2p.wallet.databinding.ItemHistoryMoonpayTransactionBinding
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class HistorySellTransactionViewHolder(
    parent: ViewGroup,
    private val onHistoryClicked: (HistoryItem) -> Unit,
    private val binding: ItemHistoryMoonpayTransactionBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {
    fun onBind(item: HistoryItem.MoonpayTransactionItem) = with(binding) {
        root.setOnClickListener { onHistoryClicked.invoke(item) }
        imageViewTransactionStatusIcon.apply {
            setImageResource(item.statusIconRes)
            setBackgroundResource(item.statusBackgroundRes)
            imageTintList = ColorStateList.valueOf(getColor(item.statusIconColor))
        }
        startAmountView.apply {
            title = item.titleStatus
            subtitle = item.subtitleReceiver
        }
        endAmountView.apply {
            topValue = item.endTopValue
            bottomValue = null // hide SOL amount
        }
    }
}

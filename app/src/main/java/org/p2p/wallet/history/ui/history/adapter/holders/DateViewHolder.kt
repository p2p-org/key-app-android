package org.p2p.wallet.history.ui.history.adapter.holders

import android.view.ViewGroup
import org.p2p.wallet.common.date.toDateString
import org.p2p.wallet.databinding.ItemHistoryDateBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class DateViewHolder(
    parent: ViewGroup,
    private val binding: ItemHistoryDateBinding = parent.inflateViewBinding(attachToRoot = false)
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(operationOrDate: HistoryItem.DateItem) {
        val date = operationOrDate.date
        binding.dateTextView.text = date.toDateString(binding.context)
    }
}

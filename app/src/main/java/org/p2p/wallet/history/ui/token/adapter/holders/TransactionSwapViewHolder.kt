package org.p2p.wallet.history.ui.token.adapter.holders

import android.annotation.SuppressLint
import android.view.ViewGroup
import timber.log.Timber
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.databinding.ItemTransactionSwapBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.getStatusIcon
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class TransactionSwapViewHolder(
    parent: ViewGroup,
    private val glideManager: GlideManager,
    private val onTransactionClicked: (HistoryTransaction) -> Unit,
    private val binding: ItemTransactionSwapBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(item: HistoryItem.TransactionItem) {
        if (item.transaction is HistoryTransaction.Swap) {
            showSwapTransaction(item.transaction)
        } else {
            Timber.e(IllegalArgumentException("Unsupported transaction type for this ViewHolder: $item"))
        }
        itemView.setOnClickListener { onTransactionClicked(item.transaction) }
    }

    @SuppressLint("SetTextI18n")
    private fun showSwapTransaction(transaction: HistoryTransaction.Swap) {
        with(binding) {
            transactionTokenImageView.setSourceAndDestinationImages(
                glideManager,
                transaction.sourceIconUrl,
                transaction.destinationIconUrl
            )

            with(transactionData) {
                startAmountView.title = "${transaction.sourceSymbol} to ${transaction.destinationSymbol}"
                startAmountView.subtitle = getString(transaction.getTypeName())
                endAmountView.topValue = "+${transaction.getDestinationTotal()}"
                endAmountView.setTopValueTextColor(getColor(transaction.getTextColor()))
                endAmountView.bottomValue = "-${transaction.getSourceTotal()}"
            }
        }
        setStatus(transaction)
    }

    private fun setStatus(transaction: HistoryTransaction) = with(binding) {
        transactionData.startAmountView.setSubtitleDrawable(left = transaction.status.getStatusIcon() ?: 0)
    }
}

package org.p2p.wallet.history.ui.token.adapter.holders

import android.annotation.SuppressLint
import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemTransactionSwapBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.setStatus
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import timber.log.Timber

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
                endAmountView.usdAmount = transaction.getReceivedUsdAmount()
                endAmountView.setTokenAmountTextColor(getColor(R.color.color_green))
                endAmountView.tokenAmount = "${transaction.amountB} ${transaction.destinationSymbol}"
                startAmountView.title = getString(R.string.transaction_history_swap)
                startAmountView.subtitle = "${transaction.sourceSymbol} to ${transaction.destinationSymbol}"
            }
        }
        setStatus(transaction)
    }

    private fun setStatus(transaction: HistoryTransaction) {
        binding.transactionTokenImageView.setStatus(transaction.status)
    }
}

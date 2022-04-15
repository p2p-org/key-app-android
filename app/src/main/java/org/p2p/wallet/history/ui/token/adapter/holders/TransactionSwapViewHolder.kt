package org.p2p.wallet.history.ui.token.adapter.holders

import android.annotation.SuppressLint
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.common.date.toTimeString
import org.p2p.wallet.databinding.ItemTransactionSwapBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

class TransactionSwapViewHolder(
    parent: ViewGroup,
    private val onTransactionClicked: (HistoryTransaction) -> Unit,
    private val binding: ItemTransactionSwapBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(item: HistoryItem.TransactionItem) {
        when (item.transaction) {
            is HistoryTransaction.Swap -> showSwapTransaction(item.transaction)
        }
        itemView.setOnClickListener { onTransactionClicked(item.transaction) }
    }

    @SuppressLint("SetTextI18n")
    private fun showSwapTransaction(transaction: HistoryTransaction.Swap) {
        with(binding) {
            transactionTokenImageView.setSourceAndDestinationImages(
                transaction.sourceIconUrl,
                transaction.destinationIconUrl
            )

            with(transactionData) {
                addressTextView.text = "${transaction.sourceSymbol} to ${transaction.destinationSymbol}"
                valueTextView withTextOrGone transaction.getReceivedUsdAmount()
                totalTextView.text = "+ ${transaction.amountB} ${transaction.destinationSymbol}"
                totalTextView.setTextColor(valueTextView.context.getColor(R.color.colorGreen))
                timeTextView.text = transaction.date.toTimeString()
            }
        }
    }
}

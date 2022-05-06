package org.p2p.wallet.history.ui.token.adapter.holders

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemTransactionBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone
import timber.log.Timber

class TransactionViewHolder(
    parent: ViewGroup,
    private val onTransactionClicked: (HistoryTransaction) -> Unit,
    private val binding: ItemTransactionBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    fun onBind(item: HistoryItem.TransactionItem) {
        when (item.transaction) {
            is HistoryTransaction.Transfer -> showTransferTransaction(item.transaction)
            is HistoryTransaction.BurnOrMint -> showBurnOrMint(item.transaction)
            is HistoryTransaction.CreateAccount -> showCreateAccountTransaction(item.transaction)
            is HistoryTransaction.CloseAccount -> showCloseTransaction(item.transaction)
            is HistoryTransaction.Unknown -> showUnknownTransaction(item.transaction)
            else -> Timber.e("Unsupported transaction type for this ViewHolder: $item")
        }
        setStatus(item.transaction)
        itemView.setOnClickListener { onTransactionClicked(item.transaction) }
    }

    private fun showBurnOrMint(transaction: HistoryTransaction.BurnOrMint) {
        with(binding) {
            transactionTokenImageView.setTransactionIcon(transaction.getIcon())
            with(transactionData) {
                titleTextView.setText(transaction.getTitle())
                subtitleTextView.text = transaction.signature.cutMiddle()
                totalTextView.text = transaction.getTotal()
                valueTextView.text = transaction.getValue()
            }
        }
    }

    private fun showUnknownTransaction(transaction: HistoryTransaction.Unknown) {
        with(binding) {
            transactionTokenImageView.setTransactionIcon(R.drawable.ic_transaction_unknown)
            with(transactionData) {
                valueTextView.isVisible = false
                totalTextView.isVisible = false

                titleTextView.setText(R.string.transaction_history_unknown)
                subtitleTextView.text = transaction.signature.cutMiddle()
            }
        }
    }

    private fun showCreateAccountTransaction(transaction: HistoryTransaction.CreateAccount) {
        with(binding) {
            with(transactionData) {
                valueTextView.isVisible = false
                totalTextView.isVisible = false

                transactionTokenImageView.setTransactionIcon(R.drawable.ic_transaction_create)
                titleTextView.text = transaction.getInfo(getString(R.string.transaction_history_create))
                subtitleTextView.text = transaction.signature.cutMiddle()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCloseTransaction(transaction: HistoryTransaction.CloseAccount) {
        with(binding) {
            transactionTokenImageView.setTransactionIcon(R.drawable.ic_transaction_closed)
            with(transactionData) {
                valueTextView.isVisible = false
                totalTextView.isVisible = false

                titleTextView.text = transaction.getInfo(getString(R.string.transaction_history_closed))
                subtitleTextView.text = transaction.signature.cutMiddle()
            }
        }
    }

    private fun showTransferTransaction(transaction: HistoryTransaction.Transfer) {
        with(binding) {
            transactionTokenImageView.setTransactionIcon(transaction.getIcon())
            with(transactionData) {
                valueTextView.isVisible = true
                totalTextView.isVisible = true

                titleTextView.setText(transaction.getTypeName())
                subtitleTextView.text = transaction.getAddress()
                totalTextView.text = transaction.getTotal()
                valueTextView.setTextColor(getColor(transaction.getTextColor()))
                valueTextView.withTextOrGone(transaction.getValue())
            }
        }
    }

    private fun setStatus(transaction: HistoryTransaction) {
        val status = if (transaction is HistoryTransaction.Transfer) {
            transaction.status
        } else {
            null
        }
        binding.transactionTokenImageView.setStatus(status)
    }
}

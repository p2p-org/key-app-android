package org.p2p.wallet.history.ui.token.adapter.holders

import android.annotation.SuppressLint
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemHistoryTransactionBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.setStatus
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import timber.log.Timber

class TransactionViewHolder(
    parent: ViewGroup,
    private val onTransactionClicked: (HistoryTransaction) -> Unit,
    private val binding: ItemHistoryTransactionBinding = parent.inflateViewBinding(attachToRoot = false),
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
                startAmountView.title = getString(transaction.getTitle())
                startAmountView.subtitle = transaction.signature.cutMiddle()
                endAmountView.usdAmount = transaction.getTotal()
                endAmountView.tokenAmount = transaction.getValue()
            }
        }
    }

    private fun showUnknownTransaction(transaction: HistoryTransaction.Unknown) {
        with(binding) {
            transactionTokenImageView.setTransactionIcon(R.drawable.ic_transaction_unknown)
            with(transactionData) {
                endAmountView.tokenAmount = null
                endAmountView.usdAmount = null

                startAmountView.title = getString(R.string.transaction_history_unknown)
                startAmountView.subtitle = transaction.signature.cutMiddle()
            }
        }
    }

    private fun showCreateAccountTransaction(transaction: HistoryTransaction.CreateAccount) {
        with(binding) {
            with(transactionData) {
                endAmountView.tokenAmount = null
                endAmountView.usdAmount = null

                transactionTokenImageView.setTransactionIcon(R.drawable.ic_transaction_create)
                startAmountView.title = transaction.getInfo(getString(R.string.transaction_history_create))
                startAmountView.subtitle = transaction.signature.cutMiddle()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCloseTransaction(transaction: HistoryTransaction.CloseAccount) {
        with(binding) {
            transactionTokenImageView.setTransactionIcon(R.drawable.ic_transaction_closed)
            with(transactionData) {
                endAmountView.tokenAmount = null
                endAmountView.usdAmount = null

                startAmountView.title = transaction.getInfo(getString(R.string.transaction_history_closed))
                startAmountView.subtitle = transaction.signature.cutMiddle()
            }
        }
    }

    private fun showTransferTransaction(transaction: HistoryTransaction.Transfer) {
        with(binding) {
            transactionTokenImageView.setTransactionIcon(transaction.getIcon())
            with(transactionData) {
                startAmountView.title = getString(transaction.getTypeName())
                startAmountView.subtitle = transaction.getAddress()
                endAmountView.usdAmount = transaction.getTotal()
                endAmountView.setTokenAmountTextColor(getColor(transaction.getTextColor()))
                endAmountView.tokenAmount = transaction.getValue()
            }
        }
    }

    private fun setStatus(transaction: HistoryTransaction) {
        binding.transactionTokenImageView.setStatus(transaction.status)
    }
}

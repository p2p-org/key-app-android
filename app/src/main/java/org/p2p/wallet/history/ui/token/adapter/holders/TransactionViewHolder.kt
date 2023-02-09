package org.p2p.wallet.history.ui.token.adapter.holders

import android.annotation.SuppressLint
import android.view.ViewGroup
import timber.log.Timber
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemHistoryTransactionBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.getStatusIcon
import org.p2p.wallet.utils.viewbinding.getColor
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class TransactionViewHolder(
    parent: ViewGroup,
    private val glideManager: GlideManager,
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
            transactionTokenImageView.apply {
                transaction.getTokenIconUrl()
                    ?.also { setTokenImage(glideManager, it) }
                    ?: setTransactionIcon(transaction.getIcon())
            }
            with(transactionData) {
                startAmountView.title = getString(transaction.getTitle())
                startAmountView.subtitle = transaction.signature.cutMiddle()
                endAmountView.topValue = transaction.getTotal()
                endAmountView.bottomValue = transaction.getValue()
            }
        }
    }

    private fun showUnknownTransaction(transaction: HistoryTransaction.Unknown) {
        with(binding) {
            transactionTokenImageView.setTransactionIcon(R.drawable.ic_transaction_unknown)
            with(transactionData) {
                endAmountView.bottomValue = null
                endAmountView.topValue = null

                startAmountView.title = getString(R.string.transaction_history_unknown)
                startAmountView.subtitle = transaction.signature.cutMiddle()
            }
        }
    }

    private fun showCreateAccountTransaction(transaction: HistoryTransaction.CreateAccount) {
        with(binding) {
            transactionTokenImageView.apply {
                transaction.getTokenIconUrl()
                    ?.also { setTokenImage(glideManager, it) }
                    ?: setTransactionIcon(R.drawable.ic_transaction_create)
            }
            with(transactionData) {
                endAmountView.bottomValue = null
                endAmountView.topValue = null

                startAmountView.title = getString(R.string.transaction_history_create)
                startAmountView.subtitle = transaction.signature.cutMiddle()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCloseTransaction(transaction: HistoryTransaction.CloseAccount) {
        with(binding) {
            transactionTokenImageView.apply {
                transaction.getTokenIconUrl()?.let {
                    setTokenImage(glideManager, it)
                } ?: setTransactionIcon(R.drawable.ic_transaction_closed)
            }
            with(transactionData) {
                endAmountView.bottomValue = null
                endAmountView.topValue = null

                startAmountView.title = getString(R.string.transaction_history_closed)
                startAmountView.subtitle = transaction.signature.cutMiddle()
            }
        }
    }

    private fun showTransferTransaction(transaction: HistoryTransaction.Transfer) {
        with(binding) {
            transactionTokenImageView.apply {
                transaction.getTokenIconUrl()
                    ?.also { setTokenImage(glideManager, it) }
                    ?: setTransactionIcon(transaction.getIcon())
            }
            with(transactionData) {
                startAmountView.title = transaction.getAddress()
                startAmountView.subtitle = getString(transaction.getTypeName())
                endAmountView.topValue = transaction.getValue()
                endAmountView.setTopValueTextColor(getColor(transaction.getTextColor()))
                endAmountView.bottomValue = transaction.getTotal()
            }
        }
    }

    private fun setStatus(transaction: HistoryTransaction) = with(binding) {
        transactionData.startAmountView.setSubtitleDrawable(left = transaction.status.getStatusIcon() ?: 0)
    }
}

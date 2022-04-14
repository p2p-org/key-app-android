package org.p2p.wallet.history.ui.token.adapter.holders

import android.annotation.SuppressLint
import android.graphics.drawable.PictureDrawable
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import org.p2p.wallet.R
import org.p2p.wallet.common.date.toTimeString
import org.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.databinding.ItemTransactionBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

class TransactionViewHolder(
    parent: ViewGroup,
    private val onTransactionClicked: (HistoryTransaction) -> Unit,
    private val binding: ItemTransactionBinding = parent.inflateViewBinding(attachToRoot = false),
) : HistoryTransactionViewHolder(binding.root) {

    private val requestBuilder: RequestBuilder<PictureDrawable> =
        Glide.with(binding.root.context)
            .`as`(PictureDrawable::class.java)
            .listener(SvgSoftwareLayerSetter())

    fun onBind(item: HistoryItem.TransactionItem) {
        when (item.transaction) {
            is HistoryTransaction.Transfer -> showTransferTransaction(item.transaction)
            is HistoryTransaction.BurnOrMint -> showBurnOrMint(item.transaction)
            is HistoryTransaction.CreateAccount -> showCreateAccountTransaction(item.transaction)
            is HistoryTransaction.CloseAccount -> showCloseTransaction(item.transaction)
            is HistoryTransaction.Unknown -> showUnknownTransaction(item.transaction)
        }
        itemView.setOnClickListener { onTransactionClicked(item.transaction) }
    }

    private fun showBurnOrMint(transaction: HistoryTransaction.BurnOrMint) {
        with(binding) {
            tokenImageView.setImageResource(transaction.getIcon())
            with(transactionData) {
                addressTextView.text = transaction.signature.cutMiddle()
                timeTextView.text = transaction.date.toTimeString()
                totalTextView.text = transaction.getTotal()
                valueTextView.text = transaction.getValue()
            }
        }
    }

    private fun showUnknownTransaction(transaction: HistoryTransaction.Unknown) {
        with(binding) {
            tokenImageView.setImageResource(R.drawable.ic_no_money)
            with(transactionData) {
                valueTextView.isVisible = false
                totalTextView.isVisible = false

                addressTextView.text = transaction.signature.cutMiddle()
                timeTextView.text = transaction.date.toTimeString()
            }
        }
    }

    private fun showCreateAccountTransaction(transaction: HistoryTransaction.CreateAccount) {
        with(binding) {
            with(transactionData) {
                valueTextView.isVisible = false
                totalTextView.isVisible = false

                tokenImageView.setImageResource(R.drawable.ic_wallet_gray)
                addressTextView.text = transaction.signature.cutMiddle()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCloseTransaction(transaction: HistoryTransaction.CloseAccount) {
        with(binding) {
            tokenImageView.setImageResource(R.drawable.ic_trash)
            with(transactionData) {
                valueTextView.isVisible = false
                totalTextView.isVisible = false


                addressTextView.text = transaction.getInfo()
                timeTextView.text = transaction.date.toTimeString()
            }
        }
    }

    private fun showTransferTransaction(transaction: HistoryTransaction.Transfer) {
        with(binding) {
            tokenImageView.setImageResource(transaction.getIcon())
            with(transactionData) {
                valueTextView.isVisible = true
                totalTextView.isVisible = true


                addressTextView.text = transaction.getAddress()
                timeTextView.text = transaction.date.toTimeString()
                valueTextView withTextOrGone transaction.getValue()
                totalTextView.text = transaction.getTotal()
                totalTextView.setTextColor(transaction.getTextColor(valueTextView.context))
            }
        }
    }
}

package org.p2p.wallet.history.ui.history.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.wallet.R
import org.p2p.wallet.common.date.toTimeString
import org.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.databinding.ItemTransactionBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.dip
import org.p2p.wallet.utils.withTextOrGone

class TransactionViewHolder(
    binding: ItemTransactionBinding,
    private val onTransactionClicked: (HistoryTransaction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val IMAGE_SIZE = 24
    }

    constructor(parent: ViewGroup, onTransactionClicked: (HistoryTransaction) -> Unit) : this(
        ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onTransactionClicked
    )

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    private val tokenImageView = binding.tokenImageView
    private val addressTextView = binding.addressTextView
    private val timeTextView = binding.timeTextView
    private val valueTextView = binding.valueTextView
    private val totalTextView = binding.totalTextView
    private val swapView = binding.swapView
    private val sourceImageView = binding.sourceImageView
    private val destinationImageView = binding.destinationImageView

    fun onBind(item: HistoryItem.TransactionItem) {
        when (item.transaction) {
            is HistoryTransaction.Transfer -> showTransferTransaction(item.transaction)
            is HistoryTransaction.Swap -> showSwapTransaction(item.transaction)
            is HistoryTransaction.BurnOrMint -> showBurnOrMint(item.transaction)
            is HistoryTransaction.CreateAccount -> showCreateAccountTransaction(item.transaction)
            is HistoryTransaction.CloseAccount -> showCloseTransaction(item.transaction)
            is HistoryTransaction.Unknown -> showUnknownTransaction(item.transaction)
        }
        itemView.setOnClickListener { onTransactionClicked(item.transaction) }
    }

    private fun showBurnOrMint(transaction: HistoryTransaction.BurnOrMint) {
        tokenImageView.isVisible = true
        swapView.isVisible = false

        tokenImageView.setImageResource(transaction.getIcon())
        addressTextView.text = transaction.signature.cutMiddle()
        timeTextView.text = transaction.date.toTimeString()
        totalTextView.text = transaction.getTotal()
        valueTextView.text = transaction.getValue()
    }

    private fun showUnknownTransaction(transaction: HistoryTransaction.Unknown) {
        tokenImageView.isVisible = true
        swapView.isVisible = false
        valueTextView.isVisible = false
        totalTextView.isVisible = false

        tokenImageView.setImageResource(R.drawable.ic_no_money)
        addressTextView.text = transaction.signature.cutMiddle()
        timeTextView.text = transaction.date.toTimeString()
    }

    private fun showCreateAccountTransaction(transaction: HistoryTransaction.CreateAccount) {
        tokenImageView.isVisible = true
        swapView.isVisible = false
        valueTextView.isVisible = false
        totalTextView.isVisible = false

        tokenImageView.setImageResource(R.drawable.ic_wallet_gray)
        addressTextView.text = transaction.signature.cutMiddle()
    }

    @SuppressLint("SetTextI18n")
    private fun showCloseTransaction(transaction: HistoryTransaction.CloseAccount) {
        tokenImageView.isVisible = true
        swapView.isVisible = false
        valueTextView.isVisible = false
        totalTextView.isVisible = false

        tokenImageView.setImageResource(R.drawable.ic_trash)
        addressTextView.text = transaction.getInfo()
        timeTextView.text = transaction.date.toTimeString()
    }

    @SuppressLint("SetTextI18n")
    private fun showSwapTransaction(transaction: HistoryTransaction.Swap) {
        tokenImageView.isVisible = false
        swapView.isVisible = true
        valueTextView.isVisible = true
        totalTextView.isVisible = true

        loadImage(sourceImageView, transaction.sourceIconUrl)
        loadImage(destinationImageView, transaction.destinationIconUrl)

        addressTextView.text = "${transaction.sourceSymbol} to ${transaction.destinationSymbol}"
        valueTextView withTextOrGone transaction.getReceivedUsdAmount()
        totalTextView.text = "+ ${transaction.amountB} ${transaction.destinationSymbol}"
        totalTextView.setTextColor(valueTextView.context.getColor(R.color.colorGreen))
        timeTextView.text = transaction.date.toTimeString()
    }

    private fun showTransferTransaction(transaction: HistoryTransaction.Transfer) {
        tokenImageView.isVisible = true
        swapView.isVisible = false
        valueTextView.isVisible = true
        totalTextView.isVisible = true

        tokenImageView.setImageResource(transaction.getIcon())
        addressTextView.text = transaction.getAddress()
        timeTextView.text = transaction.date.toTimeString()
        valueTextView withTextOrGone transaction.getValue()
        totalTextView.text = transaction.getTotal()
        totalTextView.setTextColor(transaction.getTextColor(valueTextView.context))
    }

    private fun loadImage(imageView: ImageView, url: String) {
        if (url.contains(".svg")) {
            val size = imageView.context.dip(IMAGE_SIZE)
            requestBuilder.load(url)
                .apply(RequestOptions().override(size, size))
                .centerCrop()
                .into(imageView)
        } else {
            Glide.with(imageView).load(url).into(imageView)
        }
    }
}
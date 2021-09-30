package com.p2p.wallet.history.ui.main.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.p2p.wallet.R
import com.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import com.p2p.wallet.databinding.ItemTransactionBinding
import com.p2p.wallet.history.model.HistoryItem
import com.p2p.wallet.history.model.HistoryTransaction
import com.p2p.wallet.utils.cutMiddle
import com.p2p.wallet.utils.dip

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
    private val typeTextView = binding.typeTextView
    private val addressTextView = binding.addressTextView
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
            is HistoryTransaction.CloseAccount -> showCloseTransaction(item.transaction)
            is HistoryTransaction.Unknown -> showUnknownTransaction()
        }
        itemView.setOnClickListener { onTransactionClicked(item.transaction) }
    }

    private fun showBurnOrMint(transaction: HistoryTransaction.BurnOrMint) {
        tokenImageView.isVisible = true
        swapView.isVisible = false

        tokenImageView.setImageResource(transaction.getIcon())
        typeTextView.setText(transaction.getTitle())
        addressTextView.text = transaction.signature.cutMiddle()
        totalTextView.text = transaction.getTotal()
        valueTextView.text = transaction.getValue()
    }

    private fun showUnknownTransaction() {
        tokenImageView.isVisible = true
        swapView.isVisible = false
        tokenImageView.setImageResource(R.drawable.ic_no_money)
        typeTextView.setText(R.string.main_unknown)
    }

    @SuppressLint("SetTextI18n")
    private fun showCloseTransaction(transaction: HistoryTransaction.CloseAccount) {
        tokenImageView.isVisible = true
        swapView.isVisible = false
        tokenImageView.setImageResource(R.drawable.ic_trash)
        typeTextView.setText(R.string.main_close_account)
        addressTextView.text = transaction.getInfo()

        valueTextView.setTextColor(ContextCompat.getColor(valueTextView.context, R.color.colorGreen))
    }

    @SuppressLint("SetTextI18n")
    private fun showSwapTransaction(transaction: HistoryTransaction.Swap) {
        tokenImageView.isVisible = false
        swapView.isVisible = true

        loadImage(sourceImageView, transaction.sourceTokenUrl)
        loadImage(destinationImageView, transaction.destinationTokenUrl)
        typeTextView.setText(R.string.main_swap)
        valueTextView.text = "+ ${transaction.amountReceivedInUsd} $"
        totalTextView.text = "+ ${transaction.amountB} ${transaction.destinationSymbol}"
        addressTextView.text = "${transaction.sourceSymbol} to ${transaction.destinationSymbol}"
        valueTextView.setTextColor(ContextCompat.getColor(valueTextView.context, R.color.colorGreen))
    }

    private fun showTransferTransaction(transaction: HistoryTransaction.Transfer) {
        tokenImageView.isVisible = true
        swapView.isVisible = false

        tokenImageView.setImageResource(transaction.getIcon())
        typeTextView.setText(transaction.getTitle())
        addressTextView.text = transaction.getAddress()
        valueTextView.text = transaction.getValue()
        totalTextView.text = transaction.getTotal()
        valueTextView.setTextColor(transaction.getTextColor(valueTextView.context))
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
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
import com.p2p.wallet.history.model.Transaction
import com.p2p.wallet.history.model.HistoryItem
import com.p2p.wallet.history.model.TransferType
import com.p2p.wallet.utils.dip

class TransactionViewHolder(
    binding: ItemTransactionBinding,
    private val onTransactionClicked: (Transaction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val ADDRESS_SYMBOL_COUNT = 10
        private const val IMAGE_SIZE = 24
    }

    constructor(parent: ViewGroup, onTransactionClicked: (Transaction) -> Unit) : this(
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

    @SuppressLint("SetTextI18n")
    fun onBind(item: HistoryItem.TransactionItem) {
        when (item.transaction) {
            is Transaction.Transfer -> {
                val isSend = item.transaction.type == TransferType.SEND
                val iconResId = if (isSend) R.drawable.ic_transaction_send else R.drawable.ic_transaction_receive
                tokenImageView.setImageResource(iconResId)

                typeTextView.setText(if (isSend) R.string.main_transfer else R.string.main_receive)

                val address = if (isSend) {
                    "to ${cutAddress(item.transaction.destination)}"
                } else {
                    "from ${cutAddress(item.transaction.senderAddress)}"
                }
                addressTextView.text = address

                val value = if (isSend) {
                    "- ${item.transaction.getFormattedAmount()}"
                } else {
                    "+ ${item.transaction.getFormattedAmount()}"
                }
                valueTextView.text = value

                val total = if (isSend) {
                    "- ${item.transaction.getFormattedTotal()}"
                } else {
                    "+ ${item.transaction.getFormattedTotal()}"
                }

                totalTextView.text = total

                if (!isSend) {
                    valueTextView.setTextColor(ContextCompat.getColor(valueTextView.context, R.color.colorGreen))
                }
            }
            is Transaction.Swap -> {
                tokenImageView.isVisible = false
                swapView.isVisible = true

                loadImage(sourceImageView, item.transaction.sourceTokenUrl)
                loadImage(destinationImageView, item.transaction.destinationTokenUrl)
                typeTextView.setText(R.string.main_swap)
                valueTextView.text = "+ ${item.transaction.amountReceivedInUsd} $"
                totalTextView.text = "+ ${item.transaction.amountB} ${item.transaction.destinationSymbol}"
                addressTextView.text = "${item.transaction.sourceSymbol} to ${item.transaction.destinationSymbol}"
                valueTextView.setTextColor(ContextCompat.getColor(valueTextView.context, R.color.colorGreen))
            }
            is Transaction.CloseAccount -> {
                tokenImageView.setImageResource(R.drawable.ic_trash)
                typeTextView.setText(R.string.main_close_account)
                addressTextView.text = "${item.transaction.tokenSymbol} Closed"

                valueTextView.setTextColor(ContextCompat.getColor(valueTextView.context, R.color.colorGreen))
            }
            is Transaction.Unknown -> {
                tokenImageView.setImageResource(R.drawable.ic_no_money)
                typeTextView.setText(R.string.main_unknown)
            }
        }
        itemView.setOnClickListener { onTransactionClicked(item.transaction) }
    }

    @Suppress("MagicNumber")
    fun cutAddress(address: String): String {
        if (address.length < ADDRESS_SYMBOL_COUNT) {
            return address
        }

        val firstSix = address.take(4)
        val lastFour = address.takeLast(4)
        return "$firstSix...$lastFour"
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
package com.p2p.wallet.token.ui.adapter

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
import com.p2p.wallet.R
import com.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import com.p2p.wallet.databinding.ItemTransactionBinding
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.token.model.TransactionOrDateItem

class TransactionViewHolder(
    binding: ItemTransactionBinding,
    private val onTransactionClicked: (Transaction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val ADDRESS_SYMBOL_COUNT = 10
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
    fun onBind(item: TransactionOrDateItem.TransactionItem) {
        when (item.transaction) {
            is Transaction.Send -> {
                tokenImageView.setImageResource(R.drawable.ic_transaction_send)
                typeTextView.setText(R.string.main_send)
                addressTextView.text = "to ${cutAddress(item.transaction.destination)}"
                valueTextView.text = "- ${item.transaction.getFormattedAmount()}"
                totalTextView.text = "- ${item.transaction.getFormattedTotal()}"
            }
            is Transaction.Receive -> {
                tokenImageView.setImageResource(R.drawable.ic_transaction_receive)
                typeTextView.setText(R.string.main_receive)
                addressTextView.text = "from ${cutAddress(item.transaction.senderAddress)}"
                valueTextView.text = "+ ${item.transaction.getFormattedAmount()}"
                totalTextView.text = "+ ${item.transaction.getFormattedTotal()}"

                valueTextView.setTextColor(ContextCompat.getColor(valueTextView.context, R.color.colorGreen))
            }
            is Transaction.Swap -> {
                tokenImageView.isVisible = false
                swapView.isVisible = true

                loadImage(sourceImageView, item.transaction.sourceTokenUrl)
                loadImage(destinationImageView, item.transaction.destinationTokenUrl)
                typeTextView.setText(R.string.main_swap)
                valueTextView.text = "+ ${item.transaction.amountReceivedInUsd}"
                totalTextView.text = "+ ${item.transaction.amountB} ${item.transaction.destinationSymbol}"
                addressTextView.text = "${item.transaction.sourceSymbol} to ${item.transaction.destinationSymbol}"
                valueTextView.setTextColor(ContextCompat.getColor(valueTextView.context, R.color.colorGreen))
            }
            is Transaction.CloseAccount -> {
                tokenImageView.setImageResource(R.drawable.ic_trash)
                typeTextView.setText(R.string.main_close_account)
                addressTextView.text = "Closed"

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
            requestBuilder.load(url).into(imageView)
        } else {
            Glide.with(imageView).load(url).into(imageView)
        }
    }
}
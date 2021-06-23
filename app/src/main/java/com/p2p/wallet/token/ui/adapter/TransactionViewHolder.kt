package com.p2p.wallet.token.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
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

    private val tokenImageView = binding.tokenImageView
    private val typeTextView = binding.typeTextView
    private val addressTextView = binding.addressTextView
    private val valueTextView = binding.valueTextView
    private val totalTextView = binding.totalTextView

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
                tokenImageView.setImageResource(R.drawable.ic_transaction_swap)
                typeTextView.setText(R.string.main_swap)
                valueTextView.text = "+ ${item.transaction.getFormattedAmount()}"
                totalTextView.text = "+ ${item.transaction.getFormattedTotal()}"

                valueTextView.setTextColor(ContextCompat.getColor(valueTextView.context, R.color.colorGreen))
            }
            is Transaction.CloseAccount -> {
                tokenImageView.setImageResource(R.drawable.ic_trash)
                typeTextView.setText(R.string.main_close_account)
                addressTextView.text = "${item.transaction.tokenSymbol} Closed"
                totalTextView.text = "+ ${item.transaction.getFormattedTotal()}"
                valueTextView.text = "+ ${item.transaction.getFormattedAmount()}"

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
}
package com.p2p.wallet.main.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.databinding.ItemWalletBinding
import com.p2p.wallet.utils.dip

class TokenViewHolder(
    binding: ItemWalletBinding,
    private val onItemClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val LIST_TOP_MARGIN_IN_DP = 16
    }

    constructor(parent: ViewGroup, onItemClicked: (Token) -> Unit) : this(
        ItemWalletBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClicked
    )

    private val tokenImageView = binding.tokenImageView
    private val nameTextView = binding.nameTextView
    private val addressTextView = binding.addressTextView
    private val valueTextView = binding.valueTextView
    private val totalTextView = binding.totalTextView

    fun onBind(item: Token) {
        if (adapterPosition == 0) {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = itemView.dip(LIST_TOP_MARGIN_IN_DP)
        }

        Glide.with(tokenImageView).load(item.iconUrl).into(tokenImageView)
        nameTextView.text = item.tokenSymbol
        addressTextView.text = item.getFormattedAddress()
        valueTextView.text = item.getFormattedPrice()
        totalTextView.text = item.getFormattedTotal()

        itemView.setOnClickListener { onItemClicked(item) }
    }
}
package com.p2p.wallet.main.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.databinding.ItemTokenHiddenBinding

class TokenHiddenViewHolder(
    binding: ItemTokenHiddenBinding,
    private val onItemClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup, onItemClicked: (Token) -> Unit) : this(
        ItemTokenHiddenBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClicked
    )

    private val tokenImageView = binding.tokenImageView
    private val nameTextView = binding.nameTextView
    private val addressTextView = binding.addressTextView
    private val valueTextView = binding.valueTextView
    private val totalTextView = binding.totalTextView
    private val colorView = binding.colorView

    fun onBind(item: Token) {
        Glide.with(tokenImageView).load(item.iconUrl).into(tokenImageView)
        nameTextView.text = item.tokenSymbol
        addressTextView.text = item.getFormattedAddress()
        valueTextView.text = item.getFormattedPrice()
        totalTextView.text = item.getFormattedTotal()
        colorView.setBackgroundColor(ContextCompat.getColor(colorView.context, item.color))

        itemView.setOnClickListener { onItemClicked(item) }
    }
}
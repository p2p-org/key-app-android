package com.p2p.wallet.main.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.p2p.wallet.databinding.ItemTokenHiddenBinding
import com.p2p.wallet.token.model.Token

class TokenHiddenViewHolder(
    binding: ItemTokenHiddenBinding,
    private val onItemClicked: (Token) -> Unit,
    private val onEditClicked: (Token) -> Unit,
    private val onDeleteClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        onItemClicked: (Token) -> Unit,
        onEditClicked: (Token) -> Unit,
        onDeleteClicked: (Token) -> Unit
    ) : this(
        binding = ItemTokenHiddenBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onItemClicked = onItemClicked,
        onEditClicked = onEditClicked,
        onDeleteClicked = onDeleteClicked
    )

    private val tokenImageView = binding.tokenImageView
    private val nameTextView = binding.nameTextView
    private val addressTextView = binding.addressTextView
    private val valueTextView = binding.valueTextView
    private val totalTextView = binding.totalTextView
    private val colorView = binding.colorView
    private val deleteImageView = binding.deleteImageView
    private val editImageView = binding.editImageView
    private val contentView = binding.contentView

    fun onBind(item: Token) {
        if (!item.logoUrl.isNullOrEmpty()) {
            Glide.with(tokenImageView).load(item.logoUrl).into(tokenImageView)
        }
        nameTextView.text = item.tokenSymbol
        addressTextView.text = item.getFormattedAddress()
        valueTextView.text = item.getFormattedPrice()
        totalTextView.text = item.getFormattedTotal()
        colorView.setBackgroundColor(ContextCompat.getColor(colorView.context, item.color))
        deleteImageView.setImageResource(item.visibilityIcon)
        deleteImageView.setOnClickListener { onDeleteClicked(item) }
        editImageView.setOnClickListener { onEditClicked(item) }
        contentView.setOnClickListener { onItemClicked(item) }
    }
}
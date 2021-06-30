package com.p2p.wallet.main.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.p2p.wallet.databinding.ItemTokenHiddenBinding
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.main.model.VisibilityState
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
    private val hideImageView = binding.hideImageView
    private val editImageView = binding.editImageView
    private val contentView = binding.contentView

    fun onBind(item: TokenItem.Hidden, isZerosHidden: Boolean) {
        if (item.state is VisibilityState.Hidden) {
            itemView.isVisible = false
            return
        }

        itemView.isVisible = true
        val data = item.token
        if (!data.logoUrl.isNullOrEmpty()) {
            Glide.with(tokenImageView).load(data.logoUrl).into(tokenImageView)
        }
        nameTextView.text = data.tokenSymbol
        addressTextView.text = data.getFormattedAddress()
        valueTextView.text = data.getFormattedPrice()
        totalTextView.text = data.getFormattedTotal()
        colorView.setBackgroundColor(ContextCompat.getColor(colorView.context, data.color))
        hideImageView.setImageResource(data.getVisibilityIcon(isZerosHidden))
        hideImageView.setOnClickListener { onDeleteClicked(data) }
        editImageView.setOnClickListener { onEditClicked(data) }
        contentView.setOnClickListener { onItemClicked(data) }
    }
}
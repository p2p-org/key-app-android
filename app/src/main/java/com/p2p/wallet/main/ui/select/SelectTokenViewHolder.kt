package com.p2p.wallet.main.ui.select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.p2p.wallet.databinding.ItemTokenSimpleBinding
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.utils.dip

class SelectTokenViewHolder(
    binding: ItemTokenSimpleBinding,
    private val onItemClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val LIST_TOP_MARGIN_IN_DP = 16
    }

    constructor(
        parent: ViewGroup,
        onItemClicked: (Token) -> Unit
    ) : this(
        binding = ItemTokenSimpleBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onItemClicked = onItemClicked
    )

    private val tokenImageView = binding.tokenImageView
    private val symbolTextView = binding.symbolTextView
    private val nameTextView = binding.nameTextView
    private val valueTextView = binding.valueTextView
    private val totalTextView = binding.totalTextView
    private val colorView = binding.colorView

    fun onBind(item: Token) {
        if (adapterPosition == 0) {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = itemView.dip(LIST_TOP_MARGIN_IN_DP)
        }

        if (!item.logoUrl.isNullOrEmpty()) {
            Glide.with(tokenImageView).load(item.logoUrl).into(tokenImageView)
        }
        symbolTextView.text = item.tokenSymbol
        nameTextView.text = item.tokenName
        valueTextView.text = item.getFormattedPrice()
        totalTextView.text = item.getFormattedTotal()
        colorView.setBackgroundColor(ContextCompat.getColor(colorView.context, item.color))

        itemView.setOnClickListener { onItemClicked(item) }
    }
}
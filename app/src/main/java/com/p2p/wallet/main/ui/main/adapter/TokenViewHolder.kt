package com.p2p.wallet.main.ui.main.adapter

import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import com.p2p.wallet.common.recycler.SwipeLayout
import com.p2p.wallet.databinding.ItemTokenBinding
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.utils.dip

class TokenViewHolder(
    binding: ItemTokenBinding,
    private val isZerosHidden: Boolean,
    private val onItemClicked: (Token) -> Unit,
    private val onEditClicked: (Token) -> Unit,
    private val onHideClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val LIST_TOP_MARGIN_IN_DP = 16
    }

    constructor(
        parent: ViewGroup,
        isZerosHidden: Boolean,
        onItemClicked: (Token) -> Unit,
        onEditClicked: (Token) -> Unit,
        onDeleteClicked: (Token) -> Unit
    ) : this(
        binding = ItemTokenBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        isZerosHidden = isZerosHidden,
        onItemClicked = onItemClicked,
        onEditClicked = onEditClicked,
        onHideClicked = onDeleteClicked
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

    fun onBind(item: TokenAdapter.Companion.TokenAdapterItem.Shown) {
        val token = item.token

        if (token.isSOL) {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = itemView.dip(LIST_TOP_MARGIN_IN_DP)
        } else {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
        }

        (itemView as SwipeLayout).isEnabledSwipe = !token.isSOL

        if (!token.logoUrl.isNullOrEmpty()) {
            loadLogo(token.logoUrl)
        }

        nameTextView.text = token.tokenSymbol
        addressTextView.text = token.getFormattedAddress()
        valueTextView.text = token.getFormattedPrice()
        totalTextView.text = token.getFormattedTotal()
        colorView.setBackgroundColor(ContextCompat.getColor(colorView.context, token.color))

        deleteImageView.setImageResource(item.token.getVisibilityIcon(isZerosHidden))
        deleteImageView.setOnClickListener { onHideClicked(token) }
        editImageView.setOnClickListener { onEditClicked(token) }

        contentView.setOnClickListener { onItemClicked(token) }
    }

    private fun loadLogo(logoUrl: String) {
        if (logoUrl.endsWith(".svg")) {
            Glide.with(tokenImageView.context)
                .`as`(PictureDrawable::class.java)
                .fitCenter()
                .listener(SvgSoftwareLayerSetter())
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                .load(logoUrl)
                .into(tokenImageView)
        } else {
            Glide.with(tokenImageView).load(logoUrl).into(tokenImageView)
        }
    }
}
package com.p2p.wallet.main.ui.main.adapter

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import com.p2p.wallet.common.recycler.SwipeLayout
import com.p2p.wallet.databinding.ItemTokenBinding
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.utils.dip

class TokenViewHolder(
    binding: ItemTokenBinding,
    private val onItemClicked: (Token) -> Unit,
    private val onEditClicked: (Token) -> Unit,
    private val onHideClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    companion object {
        private const val LIST_TOP_MARGIN_IN_DP = 16
        private const val IMAGE_SIZE = 56
    }

    constructor(
        parent: ViewGroup,
        onItemClicked: (Token) -> Unit,
        onEditClicked: (Token) -> Unit,
        onDeleteClicked: (Token) -> Unit
    ) : this(
        binding = ItemTokenBinding.inflate(LayoutInflater.from(parent.context), parent, false),
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

    fun onBind(item: TokenItem.Shown, isZerosHidden: Boolean) {
        val token = item.token

        if (token.isSOL) {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = itemView.dip(LIST_TOP_MARGIN_IN_DP)
        } else {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
        }

        (itemView as SwipeLayout).isEnabledSwipe = !token.isSOL

        if (!token.logoUrl.isNullOrEmpty()) {
            loadImage(tokenImageView, token.logoUrl)
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

    private fun loadImage(imageView: ImageView, url: String) {
        if (url.contains(".svg")) {
            requestBuilder
                .load(Uri.parse(url))
                .apply(RequestOptions().override(IMAGE_SIZE, IMAGE_SIZE))
                .centerCrop()
                .into(imageView)
        } else {
            Glide.with(imageView).load(url).into(imageView)
        }
    }
}
package org.p2p.wallet.main.ui.select

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.databinding.ItemTokenSimpleBinding
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.utils.dip

class SelectTokenViewHolder(
    binding: ItemTokenSimpleBinding,
    private val onItemClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val LIST_TOP_MARGIN_IN_DP = 16
        private const val IMAGE_SIZE = 56
    }

    constructor(
        parent: ViewGroup,
        onItemClicked: (Token) -> Unit
    ) : this(
        binding = ItemTokenSimpleBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onItemClicked = onItemClicked
    )

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    private val tokenImageView = binding.tokenImageView
    private val wrappedImageView = binding.wrappedImageView
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
            loadImage(tokenImageView, item.logoUrl!!)
        }
        wrappedImageView.isVisible = item.isWrapped
        symbolTextView.text = item.tokenSymbol
        colorView.setBackgroundColor(ContextCompat.getColor(colorView.context, item.color))
        itemView.setOnClickListener { onItemClicked(item) }

        when (item) {
            is Token.Active -> {
                nameTextView.text = item.getFormattedAddress()
                valueTextView.text = item.getFormattedPrice()
                totalTextView.text = item.getFormattedTotal()
                valueTextView.isVisible = true
                totalTextView.isVisible = true
            }
            is Token.Other -> {
                nameTextView.text = item.tokenName
                valueTextView.isVisible = false
                totalTextView.isVisible = false
            }
        }
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
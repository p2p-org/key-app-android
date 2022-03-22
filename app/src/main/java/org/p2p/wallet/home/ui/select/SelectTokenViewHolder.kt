package org.p2p.wallet.home.ui.select

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.wallet.common.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.databinding.ItemTokenSimpleBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.dip
import java.util.Locale

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
    private val nameTextView = binding.nameTextView
    private val symbolTextView = binding.symbolTextView
    private val usdValueTextView = binding.usdValueTextView
    private val totalTextView = binding.totalTextView

    fun onBind(item: Token) {
        if (bindingAdapterPosition == 0) {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = itemView.dip(LIST_TOP_MARGIN_IN_DP)
        } else {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
        }

        if (!item.iconUrl.isNullOrEmpty()) {
            loadImage(tokenImageView, item.iconUrl!!)
        }

        nameTextView.text = item.getFormattedName()
        symbolTextView.text = item.tokenSymbol.uppercase(Locale.getDefault())
        wrappedImageView.isVisible = item.isWrapped
        itemView.setOnClickListener { onItemClicked(item) }

        when (item) {
            is Token.Active -> {
                usdValueTextView.text = item.getFormattedUsdTotal()
                totalTextView.text = item.getFormattedTotal(includeSymbol = true)
                usdValueTextView.isVisible = true
                totalTextView.isVisible = true
            }
            is Token.Other -> {
                usdValueTextView.isVisible = false
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

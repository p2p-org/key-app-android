package org.p2p.wallet.home.ui.select

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import java.util.Locale
import org.p2p.core.glide.SvgSoftwareLayerSetter
import org.p2p.core.token.Token
import org.p2p.uikit.utils.dip
import org.p2p.wallet.databinding.ItemTokenSimpleBinding

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
    private val startAmountView = binding.startAmountView
    private val endAmountView = binding.endAmountView

    fun onBind(item: Token) {
        if (bindingAdapterPosition == 0) {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = dip(LIST_TOP_MARGIN_IN_DP)
        } else {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
        }

        if (!item.iconUrl.isNullOrEmpty()) {
            loadImage(tokenImageView, item.iconUrl!!)
        }

        startAmountView.title = item.getFormattedName()
        startAmountView.subtitle = item.tokenSymbol.uppercase(Locale.getDefault())
        wrappedImageView.isVisible = item.isWrapped
        itemView.setOnClickListener { onItemClicked(item) }

        when (item) {
            is Token.Active -> {
                endAmountView.topValue = item.getFormattedUsdTotal()
                endAmountView.bottomValue = item.getFormattedTotal(includeSymbol = true)
                endAmountView.isVisible = true
            }
            is Token.Other -> {
                endAmountView.isVisible = false
            }
            is Token.Eth -> {
                // do nothing
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

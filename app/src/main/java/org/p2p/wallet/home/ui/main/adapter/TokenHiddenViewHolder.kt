package org.p2p.wallet.home.ui.main.adapter

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
import org.p2p.core.R
import org.p2p.core.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.databinding.ItemTokenHiddenBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.utils.withTextOrGone

class TokenHiddenViewHolder(
    private val binding: ItemTokenHiddenBinding,
    private val listener: HomeItemsClickListeners
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        listener: HomeItemsClickListeners
    ) : this(
        binding = ItemTokenHiddenBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

    companion object {
        private const val IMAGE_SIZE = 56
    }

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    fun onBind(item: HomeElementItem.Hidden, isZerosHidden: Boolean) = with(binding) {
        val token = item.token
        val iconUrl = token.iconUrl
        if (!iconUrl.isNullOrEmpty()) {
            loadImage(tokenImageView, iconUrl)
        }
        wrappedImageView.isVisible = token.isWrapped
        nameTextView.text = token.tokenName
        bindBalance(item)

        imageViewExposeToken.setImageResource(token.getVisibilityIcon(isZerosHidden))
        imageViewExposeToken.setOnClickListener { listener.onHideClicked(token) }

        contentView.setOnClickListener { listener.onTokenClicked(token) }
    }

    fun bindBalance(item: HomeElementItem.Hidden) {
        val token = item.token
        binding.valueTextView withTextOrGone token.getFormattedUsdTotal()
        binding.totalTextView.text = token.getFormattedTotal(includeSymbol = true)
    }

    private fun loadImage(imageView: ImageView, url: String) {
        if (url.contains(".svg")) {
            requestBuilder
                .load(Uri.parse(url))
                .apply(RequestOptions().override(IMAGE_SIZE, IMAGE_SIZE))
                .centerCrop()
                .into(imageView)
        } else {
            Glide
                .with(imageView)
                .load(url)
                .placeholder(R.drawable.ic_placeholder_image)
                .into(imageView)
        }
    }
}

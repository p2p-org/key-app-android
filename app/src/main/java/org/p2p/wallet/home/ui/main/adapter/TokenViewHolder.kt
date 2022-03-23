package org.p2p.wallet.home.ui.main.adapter

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
import org.p2p.wallet.databinding.ItemTokenBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.utils.withTextOrGone

class TokenViewHolder(
    private val binding: ItemTokenBinding,
    private val listener: OnHomeItemsClickListener
) : RecyclerView.ViewHolder(binding.root) {

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    companion object {
        private const val IMAGE_SIZE = 56
    }

    constructor(
        parent: ViewGroup,
        listener: OnHomeItemsClickListener
    ) : this(
        binding = ItemTokenBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

    fun onBind(item: HomeElementItem.Shown, isZerosHidden: Boolean) = with(binding) {
        val token = item.token

        hideImageView.isVisible = !token.isSOL

        if (!token.iconUrl.isNullOrEmpty()) {
            loadImage(tokenImageView, token.iconUrl)
        }
        wrappedImageView.isVisible = token.isWrapped
        nameTextView.text = token.tokenName
        valueTextView withTextOrGone token.getFormattedUsdTotal()
        totalTextView.text = token.getTotal(includeSymbol = true)

        hideImageView.setImageResource(item.token.getVisibilityIcon(isZerosHidden))
        hideImageView.setOnClickListener { listener.onHideClicked(token) }
        sendImageView.setOnClickListener { listener.onSendClicked(token) }

        contentView.setOnClickListener { listener.onTokenClicked(token) }
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

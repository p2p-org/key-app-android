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
import org.p2p.core.token.Token
import org.p2p.wallet.common.ui.recycler.swipe.SwipeRevealLayout
import org.p2p.wallet.databinding.ItemTokenBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.utils.withTextOrGone

private const val VIEW_ALPHA_MAX_VALUE = 0.8f

class TokenViewHolder(
    private val binding: ItemTokenBinding,
    private val listener: HomeItemsClickListeners
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var renderItem: HomeElementItem.Shown

    private val token: Token.Active
        get() = renderItem.token

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    companion object {
        private const val IMAGE_SIZE = 56
    }

    constructor(
        parent: ViewGroup,
        listener: HomeItemsClickListeners
    ) : this(
        binding = ItemTokenBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

    fun onBind(item: HomeElementItem.Shown, isZerosHidden: Boolean) = with(binding) {
        bindBalance(item)

        layoutHide.clipToOutline = false
        layoutHide.clipToPadding = false

        val iconUrl = token.iconUrl
        if (!iconUrl.isNullOrEmpty()) {
            loadImage(tokenImageView, iconUrl)
        }
        wrappedImageView.isVisible = token.isWrapped
        nameTextView.text = token.tokenName

        imageViewHideToken.setImageResource(item.token.getVisibilityIcon(isZerosHidden))
        imageViewHideToken.setOnClickListener { listener.onHideClicked(token) }

        contentView.setOnClickListener { listener.onTokenClicked(token) }

        root.setSwipeListener(object : SwipeRevealLayout.SwipeListener {
            override fun onClosed(view: SwipeRevealLayout?) {
                viewAlpha.alpha = 0f
            }

            override fun onOpened(view: SwipeRevealLayout?) {
                viewAlpha.alpha = VIEW_ALPHA_MAX_VALUE
            }

            override fun onSlide(view: SwipeRevealLayout?, slideOffset: Float) {
                viewAlpha.alpha = if (slideOffset > VIEW_ALPHA_MAX_VALUE) VIEW_ALPHA_MAX_VALUE else slideOffset
            }
        })
    }

    fun bindBalance(item: HomeElementItem.Shown) {
        renderItem = item
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

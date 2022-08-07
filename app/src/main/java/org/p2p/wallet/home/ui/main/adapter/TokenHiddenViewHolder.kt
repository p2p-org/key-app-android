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
import org.p2p.uikit.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.common.ui.recycler.swipe.SwipeRevealLayout
import org.p2p.wallet.databinding.ItemTokenHiddenBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.utils.withTextOrGone

private const val VIEW_ALPHA_MAX_VALUE = 0.8f

class TokenHiddenViewHolder(
    private val binding: ItemTokenHiddenBinding,
    private val listener: OnHomeItemsClickListener
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        listener: OnHomeItemsClickListener
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
        if (item.state is VisibilityState.Hidden) {
            itemView.isVisible = false
            return
        }

        itemView.isVisible = true
        val token = item.token
        if (!token.iconUrl.isNullOrEmpty()) {
            loadImage(tokenImageView, token.iconUrl)
        }
        wrappedImageView.isVisible = token.isWrapped
        nameTextView.text = token.tokenName
        valueTextView withTextOrGone token.getFormattedUsdTotal()
        totalTextView.text = token.getTotal(includeSymbol = true)

        imageViewExpose.setImageResource(token.getVisibilityIcon(isZerosHidden))
        imageViewExpose.setOnClickListener { listener.onHideClicked(token) }

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

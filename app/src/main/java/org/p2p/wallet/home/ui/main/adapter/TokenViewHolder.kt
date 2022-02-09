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
import org.p2p.wallet.common.ui.recycler.SwipeLayout
import org.p2p.wallet.databinding.ItemTokenBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.utils.dip
import org.p2p.wallet.utils.getColor
import org.p2p.wallet.utils.withTextOrGone

class TokenViewHolder(
    binding: ItemTokenBinding,
    private val listener: OnHomeItemsClickListener
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
        listener: OnHomeItemsClickListener
    ) : this(
        binding = ItemTokenBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener = listener
    )

    private val tokenImageView = binding.tokenImageView
    private val wrappedImageView = binding.wrappedImageView
    private val nameTextView = binding.nameTextView
    private val rateTextView = binding.rateTextView
    private val valueTextView = binding.valueTextView
    private val totalTextView = binding.totalTextView
    private val colorView = binding.colorView
    private val deleteImageView = binding.deleteImageView
    private val contentView = binding.contentView

    fun onBind(item: HomeElementItem.Shown, isZerosHidden: Boolean) {
        val token = item.token

        if (adapterPosition == 0) {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = itemView.dip(LIST_TOP_MARGIN_IN_DP)
        } else {
            (itemView.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
        }

        (itemView as SwipeLayout).isEnabledSwipe = !token.isSOL

        if (!token.iconUrl.isNullOrEmpty()) {
            loadImage(tokenImageView, token.iconUrl)
        }
        wrappedImageView.isVisible = token.isWrapped
        nameTextView.text = token.tokenName
        rateTextView withTextOrGone token.getCurrentRate()
        valueTextView withTextOrGone token.getFormattedUsdTotal()
        totalTextView.text = token.getFormattedTotal()

        colorView.setBackgroundColor(colorView.getColor(token.color))

        deleteImageView.setImageResource(item.token.getVisibilityIcon(isZerosHidden))
        deleteImageView.setOnClickListener { listener.onHideClicked(token) }

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
package org.p2p.wallet.home.ui.select

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.uikit.glide.SvgSoftwareLayerSetter
import org.p2p.uikit.utils.dip
import org.p2p.wallet.common.ui.recycler.adapter.BaseSelectionViewHolder
import org.p2p.wallet.databinding.ItemNewTokenSimpleBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import java.util.Locale

class NewSelectTokenViewHolder(
    parent: ViewGroup,
    binding: ItemNewTokenSimpleBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onItemClicked: (Token) -> Unit
) : BaseSelectionViewHolder<Token>(binding.root, onItemClicked) {

    companion object {
        private const val LIST_TOP_MARGIN_IN_DP = 16
        private const val IMAGE_SIZE = 56
    }

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    private val tokenImageView = binding.tokenImageView
    private val wrappedImageView = binding.wrappedImageView
    private val nameTextView = binding.nameTextView
    private val symbolTextView = binding.symbolTextView
    private val usdValueTextView = binding.usdValueTextView
    private val totalTextView = binding.totalTextView

    override fun onBind(item: Token, selectedItem: Token?) {
        super.onBind(item, selectedItem)
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

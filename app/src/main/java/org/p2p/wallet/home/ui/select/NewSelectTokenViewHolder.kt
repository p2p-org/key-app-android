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
import org.p2p.wallet.common.ui.recycler.adapter.BaseSelectionViewHolder
import org.p2p.wallet.databinding.ItemNewTokenSimpleBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.formatUsd
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone
import java.util.Locale

class NewSelectTokenViewHolder(
    parent: ViewGroup,
    binding: ItemNewTokenSimpleBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onItemClicked: (Token) -> Unit
) : BaseSelectionViewHolder<Token>(binding.root, onItemClicked) {

    companion object {
        private const val IMAGE_SIZE = 56
    }

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    private val tokenImageView = binding.tokenImageView
    private val wrappedImageView = binding.wrappedImageView
    private val textViewSymbol = binding.textViewSymbol
    private val textViewUsdValue = binding.textViewUsdValue
    private val checkItem = binding.imageViewCheck

    override fun onBind(item: Token, selectedItem: Token?) {
        super.onBind(item, selectedItem)
        checkItem.isVisible = item === selectedItem

        if (!item.iconUrl.isNullOrEmpty()) {
            loadImage(tokenImageView, item.iconUrl!!)
        }

        textViewSymbol.text = item.tokenSymbol.uppercase(Locale.getDefault())
        wrappedImageView.isVisible = item.isWrapped
        itemView.setOnClickListener { onItemClicked(item) }

        textViewUsdValue.withTextOrGone("$ ${item.usdRateOrZero.formatUsd()}")
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

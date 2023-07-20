package org.p2p.wallet.home.ui.select

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.core.R
import org.p2p.core.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.common.ui.recycler.adapter.BaseSelectionViewHolder
import org.p2p.wallet.databinding.ItemNewTokenSimpleBinding
import org.p2p.core.token.Token
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone
import java.util.Locale

class NewSelectTokenViewHolder(
    parent: ViewGroup,
    private val binding: ItemNewTokenSimpleBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onItemClicked: (Token) -> Unit
) : BaseSelectionViewHolder<Token>(binding.root, onItemClicked) {

    companion object {
        private const val IMAGE_SIZE = 56
    }

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    override fun onBind(item: Token, selectedItem: Token?) {
        super.onBind(item, selectedItem)
        with(binding) {
            imageViewCheck.isVisible = item.tokenSymbol == selectedItem?.tokenSymbol

            if (!item.iconUrl.isNullOrEmpty()) {
                loadImage(imageViewToken, item.iconUrl!!)
            }

            textViewTokenSymbol.text = item.tokenSymbol.uppercase(Locale.getDefault())
            imageViewWrapped.isVisible = item.isWrapped
            itemView.setOnClickListener { onItemClicked(item) }

            textViewUsdValue.withTextOrGone(item.currencyFormattedRate)
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
            Glide
                .with(imageView)
                .load(url)
                .placeholder(R.drawable.ic_placeholder_image)
                .into(imageView)
        }
    }
}

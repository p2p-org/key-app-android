package org.p2p.wallet.home.ui.main.empty.epoxy

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.glide.SvgSoftwareLayerSetter
import org.p2p.uikit.utils.getString
import org.p2p.uikit.utils.withTextOrGone
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemGetTokenBinding
import org.p2p.wallet.home.model.EmptyHomeItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.asUsd
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

data class EmptyHomePopularTokenModel(
    private val token: Token
) : ViewBindingKotlinModel<ItemGetTokenBinding>(R.layout.item_get_token) {

    private lateinit var requestBuilder: RequestBuilder<PictureDrawable>

    companion object {
        private const val IMAGE_SIZE = 56
    }

    override fun buildView(parent: ViewGroup): View {
        return super.buildView(parent)
            .also {
                requestBuilder = Glide.with(it.context)
                    .`as`(PictureDrawable::class.java)
                    .listener(SvgSoftwareLayerSetter())
            }
    }

    override fun ItemGetTokenBinding.bind() {
        if (!token.iconUrl.isNullOrEmpty()) {
            loadImage(imageViewToken, token.iconUrl!!)
        }

        textViewName.text = token.tokenName
        textViewValue.text = getString(
            if (token.isRenBTC) {
                R.string.main_popular_token_action_receive_button
            } else {
                R.string.main_popular_token_action_buy_button
            }
        )

        val rate = if (token is Token.Active) {
            token.usdRateOrZero.asUsd()
        } else null

        textViewTotal.withTextOrGone(rate)
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




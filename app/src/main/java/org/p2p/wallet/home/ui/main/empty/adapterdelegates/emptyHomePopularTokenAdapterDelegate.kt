package org.p2p.wallet.home.ui.main.empty.adapterdelegates

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemGetTokenBinding
import org.p2p.wallet.home.model.EmptyHomeItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.asUsd
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

private const val IMAGE_SIZE = 56

fun emptyHomePopularTokenAdapterDelegate(): AdapterDelegate<List<EmptyHomeItem>> =
    adapterDelegateViewBinding<EmptyHomeItem.EmptyHomePopularOneTokenItem, EmptyHomeItem, ItemGetTokenBinding>(
        viewBinding = { _, parent -> parent.inflateViewBinding(attachToRoot = false) },
        block = {
            val requestBuilder = Glide.with(context)
                .`as`(PictureDrawable::class.java)
                .listener(SvgSoftwareLayerSetter())

            bind {
                val item = item.token
                with(binding) {
                    if (!item.iconUrl.isNullOrEmpty()) {
                        requestBuilder.loadImage(imageViewToken, item.iconUrl!!)
                    }

                    textViewName.text = item.tokenName
                    textViewValue.text = getString(
                        if (item.isRenBTC) {
                            R.string.main_popular_token_action_receive_button
                        } else {
                            R.string.main_popular_token_action_buy_button
                        }
                    )

                    val rate = if (item is Token.Active) {
                        item.usdRateOrZero.asUsd()
                    } else null

                    textViewTotal.withTextOrGone(rate)
                }
            }
        }
    )

private fun RequestBuilder<*>.loadImage(imageView: ImageView, url: String) {
    if (url.contains(".svg")) {
        load(Uri.parse(url))
            .apply(
                RequestOptions().override(
                    IMAGE_SIZE,
                    IMAGE_SIZE
                )
            )
            .centerCrop()
            .into(imageView)
    } else {
        Glide.with(imageView).load(url).into(imageView)
    }
}

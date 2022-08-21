package org.p2p.wallet.home.ui.main.empty.abramovdelegates

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.uikit.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.R
import org.p2p.wallet.common.delegates.SmartDelegate
import org.p2p.wallet.databinding.ItemGetTokenBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.asUsd
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

class GetTokenDelegate(
    private val onPopularTokenClicked: (Token) -> Unit
) : SmartDelegate<Token, ItemGetTokenBinding>(
    { parent -> parent.inflateViewBinding(attachToRoot = false) }
) {

    private lateinit var requestBuilder: RequestBuilder<PictureDrawable>

    companion object {
        private const val IMAGE_SIZE = 56
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder<ItemGetTokenBinding> {
        requestBuilder = Glide.with(parent.context)
            .`as`(PictureDrawable::class.java)
            .listener(SvgSoftwareLayerSetter())
        return super.onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder<ItemGetTokenBinding>, data: Token) = with(holder.binding) {
        if (!data.iconUrl.isNullOrEmpty()) {
            loadImage(imageViewToken, data.iconUrl!!)
        }

        textViewName.text = data.tokenName
        textViewValue.text = getString(
            if (data.isRenBTC) {
                R.string.main_popular_token_action_receive_button
            } else {
                R.string.main_popular_token_action_buy_button
            }
        )

        val rate = if (data is Token.Active) {
            data.usdRateOrZero.asUsd()
        } else null

        textViewTotal.withTextOrGone(rate)

        contentView.setOnClickListener { onPopularTokenClicked(data) }
        textViewValue.setOnClickListener { onPopularTokenClicked(data) }
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

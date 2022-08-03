package org.p2p.wallet.home.ui.main.empty

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.uikit.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemGetTokenBinding
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.utils.asUsd
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

class GetTokenViewHolder(
    parent: ViewGroup,
    private val binding: ItemGetTokenBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onPopularTokenClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    companion object {
        private const val IMAGE_SIZE = 56
    }

    fun onBind(token: Token) = with(binding) {

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

        contentView.setOnClickListener { onPopularTokenClicked(token) }
        textViewValue.setOnClickListener { onPopularTokenClicked(token) }
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

package org.p2p.wallet.home.ui.main.empty

import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.core.glide.SvgSoftwareLayerSetter
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.formatFiat
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemPopularTokenBinding
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

class PopularTokenViewHolder(
    parent: ViewGroup,
    private val binding: ItemPopularTokenBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onPopularTokenClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    private companion object {
        private const val IMAGE_SIZE = 56
    }

    fun onBind(token: Token) = with(binding) {

        val tokenIcon = token.iconUrl
        if (!tokenIcon.isNullOrEmpty()) {
            loadImage(imageViewToken, tokenIcon)
        }

        textViewName.text = token.mapTokenToPopularName()
        textViewValue.text = getString(R.string.main_popular_token_action_buy_button)

        textViewTotal.withTextOrGone("$ ${token.usdRateOrZero.formatFiat()}")

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

    private fun Token.mapTokenToPopularName(): String = when (tokenSymbol) {
        Constants.USDC_SYMBOL -> "USDC"
        "BTC" -> "Bitcoin"
        "ETH" -> "Ethereum"
        else ->
            if (tokenName == "Wrapped SOL") "Solana" else tokenName
    }
}

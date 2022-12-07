package org.p2p.wallet.home.ui.new.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.core.glide.SvgSoftwareLayerSetter
import org.p2p.uikit.utils.dip
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemPickTokenNewBinding
import org.p2p.wallet.home.model.SelectTokenItem
import org.p2p.wallet.home.model.SelectableTokenRoundedState.NOT_ROUNDED
import org.p2p.wallet.home.model.SelectableTokenRoundedState.ROUNDED
import org.p2p.wallet.home.model.SelectableTokenRoundedState.BOTTOM_ROUNDED
import org.p2p.wallet.home.model.SelectableTokenRoundedState.TOP_ROUNDED
import org.p2p.wallet.home.model.Token

private const val CORNER_RADIUS_DP = 12
private const val IMAGE_SIZE = 48

class NewSelectTokenViewHolder(
    private val binding: ItemPickTokenNewBinding,
    private val onItemClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        onItemClicked: (Token) -> Unit
    ) : this(
        binding = ItemPickTokenNewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onItemClicked = onItemClicked
    )

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    fun onBind(item: SelectTokenItem.SelectableToken) {
        setCornersRadius(item)

        val token = item.token

        with(binding) {
            if (!token.iconUrl.isNullOrEmpty()) loadImage(imageViewToken, token.iconUrl)

            imageViewWrapped.isVisible = token.isWrapped
            startAmountView.title = token.tokenName
            startAmountView.subtitle = token.getFormattedTotal(includeSymbol = true)
            endAmountView.usdAmount = token.getFormattedUsdTotal()

            root.setOnClickListener { onItemClicked(token) }
        }
    }

    fun setCornersRadius(item: SelectTokenItem.SelectableToken) {
        val cornerRadiusDp = itemView.dip(CORNER_RADIUS_DP).toFloat()
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(itemView.getColor(R.color.bg_snow))

        when (item.state) {
            ROUNDED ->
                gradientDrawable.cornerRadius = cornerRadiusDp
            BOTTOM_ROUNDED ->
                gradientDrawable.setCornerRadii(0f, 0f, cornerRadiusDp, cornerRadiusDp)
            TOP_ROUNDED ->
                gradientDrawable.setCornerRadii(cornerRadiusDp, cornerRadiusDp, 0f, 0f)
            NOT_ROUNDED ->
                gradientDrawable.cornerRadius = 0f
        }

        binding.root.background = gradientDrawable
    }

    private fun GradientDrawable.setCornerRadii(
        topLeft: Float,
        topRight: Float,
        bottomLeft: Float,
        bottomRight: Float
    ) {
        cornerRadii = floatArrayOf(
            topLeft, topLeft,
            topRight, topRight,
            bottomLeft, bottomLeft,
            bottomRight, bottomRight
        )
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

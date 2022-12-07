package org.p2p.wallet.solend.ui.earn.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.core.glide.SvgSoftwareLayerSetter
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSolendEarnBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.formatToken
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val IMAGE_SIZE = 48

class SolendEarnViewHolder(
    parent: ViewGroup,
    private val binding: ItemSolendEarnBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onDepositClickListener: (SolendDepositToken) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(org.p2p.core.glide.SvgSoftwareLayerSetter())

    @SuppressLint("SetTextI18n")
    fun onBind(item: SolendDepositToken) = with(binding) {
        loadImage(tokenImageView, item.iconUrl.orEmpty())
        textViewTokenName.text = item.tokenName

        textViewApy.apply {
            val supplyInterest = item.supplyInterest
            if (supplyInterest == null) {
                text = getString(R.string.not_available)
                setTextColor(getColor(R.color.text_rose))
            } else {
                text = "${supplyInterest.scaleShort()}%"
                setTextColor(getColor(R.color.text_night))
            }
        }

        if (item is SolendDepositToken.Active) {
            textViewAmount.text = "${item.depositAmount.formatToken()} ${item.tokenSymbol}"
        } else {
            textViewAmount.text = item.tokenSymbol
        }
        root.setOnClickListener {
            onDepositClickListener(item)
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

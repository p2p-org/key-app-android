package org.p2p.wallet.solend.ui.earn.adapter

import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.uikit.glide.SvgSoftwareLayerSetter
import org.p2p.wallet.databinding.ItemSolendEarnBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val IMAGE_SIZE = 48

class SolendEarnViewHolder(
    parent: ViewGroup,
    private val binding: ItemSolendEarnBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    fun onBind(item: SolendDepositToken) {
        loadImage(binding.tokenImageView, item.iconUrl.orEmpty())
        binding.textViewTokenName.text = item.tokenName
        binding.textViewApy.text = "${item.supplyInterest.scaleShort()}%"

        if (item is SolendDepositToken.Active) {
            binding.textViewAmount.text = "${item.depositAmount} ${item.tokenSymbol}"
        } else {
            binding.textViewAmount.text = item.tokenSymbol
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

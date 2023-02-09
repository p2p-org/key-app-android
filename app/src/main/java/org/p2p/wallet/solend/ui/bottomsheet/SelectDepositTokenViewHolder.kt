package org.p2p.wallet.solend.ui.bottomsheet

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import org.p2p.core.glide.SvgSoftwareLayerSetter
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.common.ui.recycler.adapter.BaseSelectionViewHolder
import org.p2p.wallet.databinding.ItemSolendDepositTokenBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import java.math.BigDecimal

class SelectDepositTokenViewHolder(
    parent: ViewGroup,
    private val binding: ItemSolendDepositTokenBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onItemClicked: (SolendDepositToken) -> Unit
) : BaseSelectionViewHolder<SolendDepositToken>(binding.root, onItemClicked) {

    companion object {
        private const val IMAGE_SIZE = 48
    }

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    override fun onBind(item: SolendDepositToken, selectedItem: SolendDepositToken?) {
        super.onBind(item, selectedItem)
        with(binding) {
            val iconUrl = item.iconUrl
            if (!iconUrl.isNullOrEmpty()) {
                loadImage(imageViewToken, iconUrl)
            }

            if (item is SolendDepositToken.Active) {
                amountViewStart.title = "${item.depositAmount.formatToken()} ${item.tokenSymbol}"
            } else {
                amountViewStart.title = item.tokenSymbol
            }
            amountViewStart.subtitle = item.tokenName

            val supplyInterestToShow = item.supplyInterest ?: BigDecimal.ZERO
            amountViewEnd.topValue = "${supplyInterestToShow.scaleShort()}%"

            itemView.setOnClickListener { onItemClicked(item) }
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

package org.p2p.wallet.solend.ui.deposits.adapter

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
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSolendDepositBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.scaleShort
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import java.math.BigDecimal

private const val IMAGE_SIZE = 48

class SolendDepositViewHolder(
    parent: ViewGroup,
    private val binding: ItemSolendDepositBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onAddMoreClicked: (SolendDepositToken.Active) -> Unit,
    private val onWithdrawClicked: (SolendDepositToken.Active) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(org.p2p.core.glide.SvgSoftwareLayerSetter())

    @SuppressLint("SetTextI18n")
    fun onBind(item: SolendDepositToken.Active) = with(binding) {
        loadImage(tokenImageView, item.iconUrl.orEmpty())

        val supplyInterestToShow = item.supplyInterest ?: BigDecimal.ZERO
        textViewApy.text = getString(R.string.solend_deposits_yielding_apy, supplyInterestToShow.scaleShort())

        textViewAmountUsd.text = "$${item.depositAmount.scaleShort()}"
        textViewTokenAmount.text = "%s %s".format(item.depositAmount.scaleShort().toString(), item.tokenSymbol)

        buttonAddMore.setOnClickListener {
            onAddMoreClicked(item)
        }
        buttonWithdraw.setOnClickListener {
            onWithdrawClicked(item)
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

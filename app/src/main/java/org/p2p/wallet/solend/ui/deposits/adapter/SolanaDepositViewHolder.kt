package org.p2p.wallet.solend.ui.deposits.adapter

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
import org.p2p.wallet.databinding.ItemSolanaDepositBinding
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val IMAGE_SIZE = 48

class SolanaDepositViewHolder(
    parent: ViewGroup,
    private val binding: ItemSolanaDepositBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onAddMoreClicked: (SolendDepositToken) -> Unit,
    private val onWithdrawClicked: (SolendDepositToken) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val requestBuilder: RequestBuilder<PictureDrawable> = Glide.with(binding.root.context)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())

    fun onBind(item: SolendDepositToken) = with(binding) {
        loadImage(tokenImageView, item.iconUrl)
        textViewApy.text = getString(R.string.deposits_yielding_apy, item.earnApy)

        // TODO PWN-5020 make real impl of amount in $
        textViewAmountUsd.text = if (item is SolendDepositToken.Active) {
            "$${item.depositAmount}"
        } else {
            "$0"
        }

        if (item is SolendDepositToken.Active) {
            textViewTokenAmount.text = "%s %s".format(item.depositAmount.toString(), item.tokenSymbol)
        } else {
            textViewTokenAmount.text = item.tokenSymbol
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

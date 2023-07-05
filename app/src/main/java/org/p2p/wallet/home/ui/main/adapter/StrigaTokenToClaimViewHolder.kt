package org.p2p.wallet.home.ui.main.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.core.utils.formatFiat
import org.p2p.wallet.databinding.ItemTokenToStrigaClaimBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val IMAGE_SIZE_DP = 64

class StrigaTokenToClaimViewHolder(
    parent: ViewGroup,
    private val glideManager: GlideManager,
    private val listener: HomeItemsClickListeners,
    private val binding: ItemTokenToStrigaClaimBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: HomeElementItem.StrigaClaim) = with(binding) {
        textViewTokenName.text = item.tokenName
        textViewTokenTotal.text = "${item.amountAvailable.formatFiat()} ${item.tokenSymbol}"

        setTokenIconUrl(item.tokenIcon)
        renderClaimButton(item.isClaimInProcess)

        buttonClaim.setOnClickListener {
            listener.onStrigaClaimTokenClicked(item)
        }
    }

    private fun renderClaimButton(isClaiming: Boolean) {
        with(binding.buttonClaim) {
            setLoading(isClaiming)
            isEnabled = !isClaiming
        }
    }

    private fun setTokenIconUrl(tokenIconUrl: String?) {
        glideManager.load(
            imageView = binding.imageViewToken,
            url = tokenIconUrl,
            size = IMAGE_SIZE_DP,
            circleCrop = true
        )
    }
}

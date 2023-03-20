package org.p2p.wallet.home.ui.main.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemTokenToClaimBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val IMAGE_SIZE_DP = 64

class TokenToClaimViewHolder(
    parent: ViewGroup,
    private val glideManager: GlideManager,
    private val listener: OnHomeItemsClickListener,
    private val binding: ItemTokenToClaimBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: HomeElementItem.Claim) = with(binding) {
        val token = item.token
        textViewTokenName.text = token.tokenName
        textViewTokenTotal.text = token.getFormattedTotal(includeSymbol = true)
        setTokenIconUrl(token.iconUrl)
        val canBeClaimed = item.isClaimEnabled
        setClaimButtonEnabled(isEnabled = canBeClaimed)
        if (canBeClaimed) {
            contentView.setOnClickListener { listener.onClaimTokenClicked(token) }
            buttonClaim.setOnClickListener { listener.onClaimTokenClicked(token) }
        }
    }

    private fun setClaimButtonEnabled(isEnabled: Boolean) {
        binding.buttonClaim.isEnabled = isEnabled
        with(binding.buttonClaim) {
            if (isEnabled) {
                setText(R.string.bridge_claim_button_text)
                setTextColorRes(R.color.text_snow)
                setBackgroundColor(getColor(R.color.bg_night))
            } else {
                setText(R.string.bridge_claiming_button_text)
                setTextColorRes(R.color.text_mountain)
                setBackgroundColor(getColor(R.color.bg_rain))
            }
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

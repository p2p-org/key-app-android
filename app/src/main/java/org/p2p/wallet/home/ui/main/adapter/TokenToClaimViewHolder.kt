package org.p2p.wallet.home.ui.main.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemTokenToClaimBinding
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class TokenToClaimViewHolder(
    parent: ViewGroup,
    private val glideManager: GlideManager,
    private val listener: OnHomeItemsClickListener,
    private val binding: ItemTokenToClaimBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: HomeElementItem.Claim) = with(binding) {
        val canBeClaimed = item.claimStatus
        // TODO add binding from token
        setClaimButtonEnabled(isEnabled = canBeClaimed)
        contentView.setOnClickListener { listener.onClaimTokenClicked() }
    }

    private fun setClaimButtonEnabled(isEnabled: Boolean) {
        binding.buttonClaim.isEnabled = isEnabled
        with(binding.buttonClaim) {
            if (isEnabled) {
                setText(R.string.claim_button_text)
                setTextColor(getColor(R.color.text_snow))
                setBackgroundColor(getColor(R.color.bg_night))
            } else {
                setText(R.string.claiming_button_text)
                setTextColor(getColor(R.color.text_mountain))
                setBackgroundColor(getColor(R.color.bg_rain))
            }
        }
    }
}

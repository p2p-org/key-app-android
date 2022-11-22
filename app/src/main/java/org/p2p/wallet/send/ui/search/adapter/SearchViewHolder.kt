package org.p2p.wallet.send.ui.search.adapter

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.databinding.ItemSearchBinding
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.toDp
import org.p2p.wallet.utils.viewbinding.getDrawable
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone
import timber.log.Timber

class SearchViewHolder(
    parent: ViewGroup,
    private val onItemClicked: (SearchResult) -> Unit,
    private val binding: ItemSearchBinding = parent.inflateViewBinding(attachToRoot = false),
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle
) : RecyclerView.ViewHolder(binding.root) {

    val iconPadding = 12.toDp()

    fun onBind(item: SearchResult) {
        when (item) {
            is SearchResult.UsernameFound -> renderFull(item)
            is SearchResult.AddressOnly -> renderAddressOnly(item)
            is SearchResult.EmptyBalance -> renderEmptyBalance(item)
            // do nothing, no wrong type should be in search view
            is SearchResult.InvalidAddress -> Timber.w("Received SearchResult.Wrong in unexpected place")
        }

        itemView.setOnClickListener { onItemClicked(item) }
    }

    private fun renderFull(item: SearchResult.UsernameFound) {
        @DrawableRes
        val imageResource: Int
        with(binding) {
            if (item.username.endsWith(usernameDomainFeatureToggle.value)) {
                walletImageView.background = null
                imageResource = R.drawable.ic_key_app_circle
            } else {
                walletImageView.background = getDrawable(R.drawable.bg_app_rounded)
                imageResource = R.drawable.ic_wallet_gray
            }

            Glide.with(root)
                .load(imageResource)
                .circleCrop()
                .into(walletImageView)

            topTextView.text = item.username
            bottomTextView withTextOrGone item.addressState.address.cutEnd()
            bottomTextView.setTextColorRes(R.color.backgroundDisabled)
        }
    }

    private fun renderAddressOnly(item: SearchResult.AddressOnly) {
        with(binding) {
            if (item.addressState.networkType == NetworkType.BITCOIN) {
                walletImageView.setImageResource(R.drawable.ic_btc)
                walletImageView.setPadding(0, 0, 0, 0)
            } else {
                walletImageView.setImageResource(R.drawable.ic_search_wallet)
                walletImageView.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
            }
            topTextView.text = item.addressState.address.cutEnd()
            bottomTextView.isVisible = false
        }
    }

    private fun renderEmptyBalance(item: SearchResult.EmptyBalance) {
        with(binding) {
            topTextView.text = item.addressState.address.cutEnd()
            bottomTextView.setText(R.string.send_caution_empty_balance)
            bottomTextView.setTextColorRes(R.color.systemWarningMain)
        }
    }
}

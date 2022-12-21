package org.p2p.wallet.send.ui.search.adapter

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.databinding.ItemSearchBinding
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.CUT_USERNAME_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutEnd
import org.p2p.wallet.utils.DateTimeUtils
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.toPx
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone
import timber.log.Timber

class SearchViewHolder(
    parent: ViewGroup,
    private val onItemClicked: (SearchResult) -> Unit,
    private val binding: ItemSearchBinding = parent.inflateViewBinding(attachToRoot = false),
    private val usernameDomainFeatureToggle: UsernameDomainFeatureToggle
) : RecyclerView.ViewHolder(binding.root) {

    val iconPadding = 12.toPx()

    fun onBind(item: SearchResult) {
        when (item) {
            is SearchResult.UsernameFound -> renderFull(item)
            is SearchResult.AddressOnly -> renderAddressOnly(item)
            is SearchResult.EmptyBalance -> renderEmptyBalance(item)
            // do nothing, no wrong type should be in search view
            else -> Timber.w("Received SearchResult.Wrong in unexpected place")
        }

        itemView.setOnClickListener { onItemClicked(item) }
    }

    private fun renderFull(item: SearchResult.UsernameFound) {
        @DrawableRes
        val imageResource: Int
        with(binding) {
            if (item.username.endsWith(usernameDomainFeatureToggle.value)) {
                imageResource = R.drawable.ic_key_app_circle
                walletImageView.setPadding(0, 0, 0, 0)
            } else {
                imageResource = R.drawable.ic_search_wallet
                walletImageView.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
            }

            walletImageView.setImageResource(imageResource)

            textViewTop.text = item.username
            textViewBottom withTextOrGone item.addressState.address.cutMiddle(CUT_USERNAME_SYMBOLS_COUNT)
            textViewBottom.setTextColorRes(R.color.backgroundDisabled)
            textViewDate.withTextOrGone(item.date?.time?.let { DateTimeUtils.getDateRelatedFormatted(it, context) })
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
            textViewTop.text = item.addressState.address.cutMiddle(CUT_USERNAME_SYMBOLS_COUNT)
            textViewBottom.isVisible = false
            textViewDate.withTextOrGone(item.date?.time?.let { DateTimeUtils.getDateRelatedFormatted(it, context) })
        }
    }

    private fun renderEmptyBalance(item: SearchResult.EmptyBalance) {
        with(binding) {
            textViewTop.text = item.addressState.address.cutMiddle(CUT_USERNAME_SYMBOLS_COUNT)
            textViewBottom.isVisible = false
        }
    }
}

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
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.CUT_USERNAME_SYMBOLS_COUNT
import org.p2p.wallet.utils.DateTimeUtils
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.toPx
import org.p2p.wallet.utils.viewbinding.context
import org.p2p.wallet.utils.viewbinding.getString
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
                textViewTop.text = "@${item.username}"
                textViewBottom.isVisible = false
            } else {
                imageResource = R.drawable.ic_search_wallet
                walletImageView.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
                textViewTop.text = item.username
                textViewBottom.withTextOrGone(item.addressState.address.cutMiddle(CUT_USERNAME_SYMBOLS_COUNT))
            }

            walletImageView.setImageResource(imageResource)

            textViewBottom.setTextColorRes(R.color.bg_mountain)
            textViewDate.withTextOrGone(item.date?.time?.let { DateTimeUtils.getDateRelatedFormatted(it, context) })
        }
    }

    private fun renderAddressOnly(item: SearchResult.AddressOnly) {
        with(binding) {
            val imageIcon = item.sourceToken?.iconUrl
            val description: String?
            val imageObject: Any = if (imageIcon != null) {
                walletImageView.apply {
                    setPadding(0, 0, 0, 0)
                }
                description = getString(
                    R.string.search_no_other_tokens_description,
                    item.sourceToken.tokenSymbol
                )
                imageIcon
            } else {
                walletImageView.apply {
                    setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
                }
                description = null
                R.drawable.ic_search_wallet
            }
            textViewTop.text = item.addressState.address.cutMiddle(CUT_USERNAME_SYMBOLS_COUNT)
            textViewBottom.withTextOrGone(description)
            textViewDate.withTextOrGone(item.date?.time?.let { DateTimeUtils.getDateRelatedFormatted(it, context) })
            Glide.with(root)
                .load(imageObject)
                .circleCrop()
                .into(walletImageView)
        }
    }
}

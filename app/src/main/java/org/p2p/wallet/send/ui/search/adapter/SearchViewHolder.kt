package org.p2p.wallet.send.ui.search.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.UsernameDomainFeatureToggle
import org.p2p.wallet.databinding.ItemSearchBinding
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.cutEnd
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

    private val topTextView = binding.topTextView
    private val bottomTextView = binding.bottomTextView

    fun onBind(item: SearchResult) {
        when (item) {
            is SearchResult.Full -> {
                if (item.username.endsWith(usernameDomainFeatureToggle.value)) {
                    binding.walletImageView.background = null
                    binding.walletImageView.setImageResource(R.drawable.ic_key_app_circle)
                } else {
                    binding.walletImageView.background = binding.getDrawable(R.drawable.bg_app_rounded)
                    binding.walletImageView.setImageResource(R.drawable.ic_wallet_gray)
                }

                topTextView.text = item.username
                bottomTextView.withTextOrGone(item.addressState.address.cutEnd())
                bottomTextView.setTextColor(bottomTextView.context.getColor(R.color.backgroundDisabled))
            }
            is SearchResult.AddressOnly -> {
                topTextView.text = item.addressState.address.cutEnd()
                bottomTextView.isVisible = false
            }
            is SearchResult.EmptyBalance -> {
                topTextView.text = item.addressState.address.cutEnd()
                val caution = bottomTextView.context.getString(R.string.send_caution_empty_balance)
                bottomTextView.withTextOrGone(caution)
                val warningColor = bottomTextView.context.getColor(R.color.systemWarningMain)
                bottomTextView.setTextColor(warningColor)
            }
            is SearchResult.Wrong -> {
                Timber.w("Received SearchResult.Wrong in unexpected place")
                // do nothing, no wrong type should be in search view
            }
        }

        itemView.setOnClickListener { onItemClicked(item) }
    }
}

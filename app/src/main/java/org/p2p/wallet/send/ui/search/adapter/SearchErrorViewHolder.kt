package org.p2p.wallet.send.ui.search.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.bumptech.glide.Glide
import timber.log.Timber
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSearchInvalidResultBinding
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.toPx
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

class SearchErrorViewHolder(
    parent: ViewGroup,
    private val binding: ItemSearchInvalidResultBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    private val iconPadding = 12.toPx()

    fun onBind(item: SearchResult) {
        with(binding) {
            textViewAddress.text = item.addressState.address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)

            when (item) {
                is SearchResult.OwnAddressError -> {
                    val description = if (item.directToken != null) {
                        loadTokenIcon(item.directToken.iconUrl)
                        getString(R.string.search_your_token_description, item.directToken.symbol)
                    } else {
                        showWalletIcon()
                        getString(R.string.search_yourself_description)
                    }
                    textViewDescription.withTextOrGone(description)
                    textViewError.setText(R.string.search_yourself_error)
                }
                is SearchResult.InvalidDirectAddress -> {
                    val description = getString(
                        R.string.search_no_other_tokens_description,
                        item.directToken.symbol
                    )
                    textViewDescription.withTextOrGone(description)
                    textViewError.setText(R.string.search_no_other_tokens_error)
                    loadTokenIcon(item.directToken.iconUrl)
                }
                // We are expecting no to get any other types of results for now
                else -> {
                    Timber.w("Unexpected search result type: $item")
                }
            }
        }
    }

    private fun showWalletIcon() {
        binding.frameWalletImageView.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
        binding.imageViewWallet.apply {
            alpha = 0.3f
            setImageResource(R.drawable.ic_search_wallet)
        }
    }

    private fun loadTokenIcon(iconUrl: String?) {
        binding.frameWalletImageView.setPadding(0, 0, 0, 0)
        with(binding.imageViewWallet) {
            alpha = 1f

            Glide.with(this)
                .load(iconUrl)
                .circleCrop()
                .error(R.drawable.ic_placeholder_v2)
                .placeholder(R.drawable.ic_placeholder_v2)
                .into(this)
        }
    }
}

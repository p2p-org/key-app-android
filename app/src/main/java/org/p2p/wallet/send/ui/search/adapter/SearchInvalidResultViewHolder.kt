package org.p2p.wallet.send.ui.search.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSearchInvalidResultBinding
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.CUT_SEVEN_SYMBOLS
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.toPx
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

class SearchInvalidResultViewHolder(
    parent: ViewGroup,
    private val binding: ItemSearchInvalidResultBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    val iconPadding = 12.toPx()

    fun onBind(item: SearchResult.InvalidResult) {
        with(binding) {
            textViewAddress.text = item.addressState.address.cutMiddle(CUT_SEVEN_SYMBOLS)
            textViewDescription.withTextOrGone(item.description)
            textViewError.text = item.errorMessage

            val imageIcon = item.tokenData?.iconUrl
            val imageObject: Any = if (imageIcon != null) {
                imageViewWallet.apply {
                    setPadding(0, 0, 0, 0)
                    alpha = 1f
                }
                imageIcon
            } else {
                imageViewWallet.apply {
                    setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
                    alpha = 0.3f
                }
                R.drawable.ic_search_wallet
            }

            Glide.with(root)
                .load(imageObject)
                .circleCrop()
                .into(imageViewWallet)
        }
    }
}

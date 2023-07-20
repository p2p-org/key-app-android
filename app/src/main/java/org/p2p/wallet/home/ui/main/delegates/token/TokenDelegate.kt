package org.p2p.wallet.home.ui.main.delegates.token

import androidx.core.view.isVisible
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.AdapterDelegateViewBindingViewHolder
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.databinding.ItemTokenBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

const val DIFF_FIELD_TOGGLE_BUTTON = "DIFF_FIELD_TOGGLE_BUTTON"
const val DIFF_FIELD_TOKEN_BALANCE = "DIFF_FIELD_TOKEN_BALANCE"
const val DIFF_FIELD_HIDDEN_TOKEN_BALANCE = "DIFF_FIELD_HIDDEN_TOKEN_BALANCE"
const val DIFF_FIELD_TITLE = "DIFF_FIELD_TITLE"

private typealias BindListener = ((binding: ItemTokenBinding, item: TokenCellModel) -> Unit)

private val inflateViewBinding = { inflater: LayoutInflater, parent: ViewGroup ->
    inflater.inflateViewBinding<ItemTokenBinding>(root = parent, attachToRoot = false)
}

fun tokenDelegate(
    glideManager: GlideManager,
    onBindListener: BindListener? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<TokenCellModel, AnyCellItem, ItemTokenBinding>(
        viewBinding = inflateViewBinding
    ) {
        bind { payloads ->
            @Suppress("UNCHECKED_CAST")
            val diffFields = payloads.firstOrNull() as? Set<String>

            if (diffFields.isNullOrEmpty()) {
                onBind(glideManager)
            } else {
                diffFields.forEach { field ->
                    when (field) {
                        DIFF_FIELD_TOKEN_BALANCE -> bindBalance()
                    }
                }
            }
            onBindListener?.invoke(binding, item)
        }
    }

private fun AdapterDelegateViewBindingViewHolder<TokenCellModel, ItemTokenBinding>.onBind(
    glideManager: GlideManager,
) = with(binding) {
    bindBalance()

    val iconUrl = item.iconUrl
    if (!iconUrl.isNullOrEmpty()) {
        glideManager.load(
            imageView = tokenImageView,
            url = iconUrl,
            circleCrop = true
        )
    }
    wrappedImageView.isVisible = item.isWrapped
    nameTextView.text = item.tokenName
}

private fun AdapterDelegateViewBindingViewHolder<TokenCellModel, ItemTokenBinding>.bindBalance() {
    binding.valueTextView withTextOrGone item.formattedUsdTotal
    binding.totalTextView.text = item.formattedTotal
}

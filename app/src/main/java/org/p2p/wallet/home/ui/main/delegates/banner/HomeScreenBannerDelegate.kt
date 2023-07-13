package org.p2p.wallet.home.ui.main.delegates.banner

import androidx.core.view.isVisible
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.AdapterDelegateViewBindingViewHolder
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.getString
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.wallet.databinding.ItemHomeBannerBinding
import org.p2p.wallet.home.model.HomeScreenBanner
import org.p2p.wallet.kyc.model.StrigaBanner

private typealias ItemBinding = ItemHomeBannerBinding

/**
 * Usage example:
 * ```
 * // use type T : HomeScreenBanner
 * homeScreenBannerDelegate<T> { binding, item: T ->
 *     with(binding) {
 *         buttonAction.setOnClickListener { presenter.onStrigaBannerClicked(item) }
 *         root.setOnClickListener { presenter.onStrigaBannerClicked(item) }
 *     }
 * }
 * ```
 */
inline fun <reified T : HomeScreenBanner> homeScreenBannerDelegate(
    noinline onBindListener: ((binding: ItemBinding, item: T) -> Unit)? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<T, AnyCellItem, ItemBinding>(
        viewBinding = { inflater: LayoutInflater, parent: ViewGroup ->
            inflater.inflateViewBinding(root = parent, attachToRoot = false)
        }
    ) {
        @Suppress("UNCHECKED_CAST")
        bind {
            when (item) {
                is StrigaBanner -> (this as AdapterDelegateViewBindingViewHolder<StrigaBanner, ItemBinding>).onBind()
                else -> Unit
            }
            onBindListener?.invoke(binding, item)
        }
    }

fun AdapterDelegateViewBindingViewHolder<StrigaBanner, ItemBinding>.onBind() = with(binding) {
    val status = item.status
    textViewTitle.setText(status.bannerTitleResId)

    val subtitleText = getString(status.bannerMessageResId)
    textViewSubtitle.text = subtitleText
    textViewSubtitle.isVisible = subtitleText.isNotEmpty()

    imageViewIcon.setImageResource(status.placeholderResId)

    buttonAction.isVisible = status.actionTitleResId != null
    status.actionTitleResId?.let {
        buttonAction.setText(status.actionTitleResId)
    }

    root.background.setTint(getColor(status.backgroundTint))

    buttonAction.setLoading(item.isLoading)
}

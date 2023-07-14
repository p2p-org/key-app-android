package org.p2p.wallet.home.ui.main.delegates.striga.onramp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.AdapterDelegateViewBindingViewHolder
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.core.glide.GlideManager
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.databinding.ItemTokenToStrigaOnrampBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val IMAGE_SIZE_DP = 64

private typealias ItemModel = StrigaOnRampCellModel
private typealias ItemBinding = ItemTokenToStrigaOnrampBinding
private typealias BindListener = ((binding: ItemBinding, item: ItemModel) -> Unit)
private typealias DelegatedBinder = AdapterDelegateViewBindingViewHolder<ItemModel, ItemBinding>

private val inflateViewBinding = { inflater: LayoutInflater, parent: ViewGroup ->
    inflater.inflateViewBinding<ItemBinding>(root = parent, attachToRoot = false)
}

fun strigaOnRampTokenDelegate(
    glideManager: GlideManager,
    onBindListener: BindListener? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<ItemModel, AnyCellItem, ItemBinding>(
        viewBinding = inflateViewBinding
    ) {
        bind {
            onBind(glideManager)
            onBindListener?.invoke(binding, item)
        }
    }

@SuppressLint("SetTextI18n")
fun DelegatedBinder.onBind(
    glideManager: GlideManager
) = with(binding) {
    textViewTokenName.text = item.tokenName
    textViewTokenTotal.text = "${item.amountAvailable.formatFiat()} ${item.tokenSymbol}"

    setTokenIconUrl(glideManager, item.tokenIcon)
    renderClaimButton(item.isLoading)
}

private fun DelegatedBinder.setTokenIconUrl(
    glideManager: GlideManager,
    tokenIconUrl: String?
) {
    glideManager.load(
        imageView = binding.imageViewToken,
        url = tokenIconUrl,
        size = IMAGE_SIZE_DP,
        circleCrop = true
    )
}

private fun DelegatedBinder.renderClaimButton(isClaiming: Boolean) {
    with(binding.buttonClaim) {
        setLoading(isClaiming)
        isEnabled = !isClaiming
    }
}

package org.p2p.wallet.home.ui.main.delegates.bridgeclaim

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.AdapterDelegateViewBindingViewHolder
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.getColor
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.databinding.ItemTokenToClaimBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private const val IMAGE_SIZE_DP = 64

private typealias BindListener = ((binding: ItemTokenToClaimBinding, item: EthClaimTokenCellModel) -> Unit)

private val inflateViewBinding = { inflater: LayoutInflater, parent: ViewGroup ->
    inflater.inflateViewBinding<ItemTokenToClaimBinding>(root = parent, attachToRoot = false)
}

fun ethClaimTokenDelegate(
    glideManager: GlideManager,
    onBindListener: BindListener? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<EthClaimTokenCellModel, AnyCellItem, ItemTokenToClaimBinding>(
        viewBinding = inflateViewBinding
    ) {
        bind {
            onBind(glideManager)
            onBindListener?.invoke(binding, item)
        }
    }

fun AdapterDelegateViewBindingViewHolder<EthClaimTokenCellModel, ItemTokenToClaimBinding>.onBind(
    glideManager: GlideManager
) = with(binding) {
    textViewTokenName.text = item.tokenName
    textViewTokenTotal.text = item.formattedTotal
    glideManager.load(
        imageView = binding.imageViewToken,
        url = item.iconUrl,
        size = IMAGE_SIZE_DP,
        circleCrop = true
    )
    setClaimButtonEnabled(isEnabled = item.isClaimEnabled)
}

private fun AdapterDelegateViewBindingViewHolder<EthClaimTokenCellModel, ItemTokenToClaimBinding>.setClaimButtonEnabled(
    isEnabled: Boolean
) {
    binding.buttonClaim.isEnabled = isEnabled
    with(binding.buttonClaim) {
        text = item.buttonText
        setTextColorRes(item.buttonTextColor)
        setBackgroundColor(getColor(item.buttonBackgroundColor))
    }
}

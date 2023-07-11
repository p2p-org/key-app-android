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

private const val IMAGE_SIZE_DP = 64

private typealias BindListener = ((binding: ItemTokenToClaimBinding, item: ClaimTokenCellModel) -> Unit)

private val inflateViewBinding = { inflater: LayoutInflater, parent: ViewGroup ->
    ItemTokenToClaimBinding.inflate(inflater, parent, false)
}

fun claimTokenDelegate(
    glideManager: GlideManager,
    onBindListener: BindListener? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<ClaimTokenCellModel, AnyCellItem, ItemTokenToClaimBinding>(
        viewBinding = inflateViewBinding
    ) {
        bind {
            onBind(glideManager)
            onBindListener?.invoke(binding, item)
        }
    }

fun AdapterDelegateViewBindingViewHolder<ClaimTokenCellModel, ItemTokenToClaimBinding>.onBind(
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

private fun AdapterDelegateViewBindingViewHolder<ClaimTokenCellModel, ItemTokenToClaimBinding>.setClaimButtonEnabled(
    isEnabled: Boolean
) {
    binding.buttonClaim.isEnabled = isEnabled
    with(binding.buttonClaim) {
        text = item.buttonText
        setTextColorRes(item.buttonTextColor)
        setBackgroundColor(getColor(item.buttonBackgroundColor))
    }
}

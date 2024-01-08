package org.p2p.wallet.home.ui.main.delegates.striga.offramp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.AdapterDelegateViewBindingViewHolder
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.databinding.ItemTokenToStrigaOfframpBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

private typealias ItemModel = StrigaOffRampCellModel
private typealias ItemBinding = ItemTokenToStrigaOfframpBinding
private typealias BindListener = ((binding: ItemBinding, item: ItemModel) -> Unit)
private typealias DelegatedBinder = AdapterDelegateViewBindingViewHolder<ItemModel, ItemBinding>

private val inflateViewBinding = { inflater: LayoutInflater, parent: ViewGroup ->
    inflater.inflateViewBinding<ItemBinding>(root = parent, attachToRoot = false)
}

fun strigaOffRampTokenDelegate(
    onBindListener: BindListener? = null,
): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<ItemModel, AnyCellItem, ItemBinding>(
        viewBinding = inflateViewBinding
    ) {
        bind {
            onBind()
            onBindListener?.invoke(binding, item)
        }
    }

@SuppressLint("SetTextI18n")
fun DelegatedBinder.onBind() = with(binding) {
    textViewTokenTotal.text = "${item.amountAvailable.formatFiat()} ${item.tokenSymbol}"
    renderClaimButton(item.isLoading)
}

private fun DelegatedBinder.renderClaimButton(isClaiming: Boolean) {
    with(binding.buttonClaim) {
        setLoading(isClaiming)
        isEnabled = !isClaiming
    }
}

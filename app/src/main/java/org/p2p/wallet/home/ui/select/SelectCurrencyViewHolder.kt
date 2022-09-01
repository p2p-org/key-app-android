package org.p2p.wallet.home.ui.select

import android.view.ViewGroup
import androidx.core.view.isVisible
import org.p2p.wallet.common.ui.recycler.adapter.BaseSelectionViewHolder
import org.p2p.wallet.databinding.ItemSelectCurrencyBinding
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import java.util.Locale

class SelectCurrencyViewHolder(
    parent: ViewGroup,
    private val binding: ItemSelectCurrencyBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onItemClicked: (BuyCurrency.Currency) -> Unit
) : BaseSelectionViewHolder<BuyCurrency.Currency>(binding.root, onItemClicked) {

    override fun onBind(item: BuyCurrency.Currency, selectedItem: BuyCurrency.Currency?) {
        super.onBind(item, selectedItem)
        with(binding) {
            imageViewCheck.isVisible = item == selectedItem

            textViewCurrencySymbol.text = item.code.uppercase(Locale.getDefault())
            itemView.setOnClickListener { onItemClicked(item) }
        }
    }
}

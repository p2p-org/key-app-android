package org.p2p.wallet.home.ui.select

import androidx.core.view.isVisible
import android.view.ViewGroup
import java.util.Locale
import org.p2p.wallet.common.ui.recycler.adapter.BaseSelectionViewHolder
import org.p2p.wallet.databinding.ItemSelectCurrencyBinding
import org.p2p.wallet.moonpay.repository.sell.FiatCurrency
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SelectCurrencyViewHolder(
    parent: ViewGroup,
    private val binding: ItemSelectCurrencyBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onItemClicked: (FiatCurrency) -> Unit
) : BaseSelectionViewHolder<FiatCurrency>(binding.root, onItemClicked) {

    override fun onBind(item: FiatCurrency, selectedItem: FiatCurrency?) {
        super.onBind(item, selectedItem)
        with(binding) {
            imageViewCheck.isVisible = item == selectedItem

            textViewCurrencySymbol.text = item.abbreviation.uppercase(Locale.getDefault())
            itemView.setOnClickListener { onItemClicked(item) }
        }
    }
}

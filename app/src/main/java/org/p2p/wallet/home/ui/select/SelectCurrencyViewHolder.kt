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
    binding: ItemSelectCurrencyBinding = parent.inflateViewBinding(attachToRoot = false),
    private val onItemClicked: (BuyCurrency.Currency) -> Unit
) : BaseSelectionViewHolder<BuyCurrency.Currency>(binding.root, onItemClicked) {

    private val textViewSymbol = binding.textViewSymbol
    private val checkItem = binding.imageViewCheck

    override fun onBind(item: BuyCurrency.Currency, selectedItem: BuyCurrency.Currency?) {
        super.onBind(item, selectedItem)
        checkItem.isVisible = item === selectedItem

        textViewSymbol.text = item.code.uppercase(Locale.getDefault())
        itemView.setOnClickListener { onItemClicked(item) }
    }
}

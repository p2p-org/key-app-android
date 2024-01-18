package org.p2p.wallet.home.ui.select

import android.view.ViewGroup
import org.p2p.wallet.common.ui.recycler.adapter.BaseSingleSelectionAdapter
import org.p2p.wallet.moonpay.repository.sell.FiatCurrency

class SelectCurrencyAdapter(
    preselectedItem: FiatCurrency,
    onItemClicked: (FiatCurrency) -> Unit = {}
) : BaseSingleSelectionAdapter<FiatCurrency, SelectCurrencyViewHolder>(preselectedItem, onItemClicked) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
        onItemClicked: (FiatCurrency) -> Unit
    ): SelectCurrencyViewHolder = SelectCurrencyViewHolder(parent, onItemClicked = onItemClicked)

    override fun onBindViewHolder(
        holder: SelectCurrencyViewHolder,
        item: FiatCurrency,
        selectedItem: FiatCurrency?
    ) = holder.onBind(item, selectedItem)
}

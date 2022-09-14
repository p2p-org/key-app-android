package org.p2p.wallet.home.ui.select

import android.view.ViewGroup
import org.p2p.wallet.common.ui.recycler.adapter.BaseSingleSelectionAdapter
import org.p2p.wallet.moonpay.model.BuyCurrency

class SelectCurrencyAdapter(
    preselectedItem: BuyCurrency.Currency? = null,
    onItemClicked: (BuyCurrency.Currency) -> Unit = {}
) : BaseSingleSelectionAdapter<BuyCurrency.Currency, SelectCurrencyViewHolder>(preselectedItem, onItemClicked) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
        onItemClicked: (BuyCurrency.Currency) -> Unit
    ): SelectCurrencyViewHolder = SelectCurrencyViewHolder(parent, onItemClicked = onItemClicked)

    override fun onBindViewHolder(
        holder: SelectCurrencyViewHolder,
        item: BuyCurrency.Currency,
        selectedItem: BuyCurrency.Currency?
    ) = holder.onBind(item, selectedItem)
}

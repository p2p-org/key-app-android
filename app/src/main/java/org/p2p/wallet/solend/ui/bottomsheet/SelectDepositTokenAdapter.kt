package org.p2p.wallet.solend.ui.bottomsheet

import android.view.ViewGroup
import org.p2p.wallet.common.ui.recycler.adapter.BaseSingleSelectionAdapter
import org.p2p.wallet.solend.model.SolendDepositToken

class SelectDepositTokenAdapter(
    onTokenClicked: (SolendDepositToken) -> Unit = {}
) : BaseSingleSelectionAdapter<SolendDepositToken, SelectDepositTokenViewHolder>(
    preselectedItem = null,
    onTokenClicked
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
        onItemClicked: (SolendDepositToken) -> Unit
    ): SelectDepositTokenViewHolder = SelectDepositTokenViewHolder(parent, onItemClicked = onItemClicked)

    override fun onBindViewHolder(
        holder: SelectDepositTokenViewHolder,
        item: SolendDepositToken,
        selectedItem: SolendDepositToken?
    ) = holder.onBind(item, selectedItem)
}

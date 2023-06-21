package org.p2p.wallet.settings.ui.network

import android.view.ViewGroup
import org.p2p.wallet.common.ui.recycler.adapter.BaseSingleSelectionAdapter
import org.p2p.core.network.environment.NetworkEnvironment

class SelectNetworkAdapter(
    onItemClicked: (NetworkEnvironment) -> Unit
) : BaseSingleSelectionAdapter<NetworkEnvironment, SelectNetworkViewHolder>(onItemClicked = onItemClicked) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
        onItemClicked: (NetworkEnvironment) -> Unit
    ): SelectNetworkViewHolder = SelectNetworkViewHolder(parent, onItemClicked = onItemClicked)

    override fun onBindViewHolder(
        holder: SelectNetworkViewHolder,
        item: NetworkEnvironment,
        selectedItem: NetworkEnvironment?
    ) = holder.onBind(item, selectedItem)
}

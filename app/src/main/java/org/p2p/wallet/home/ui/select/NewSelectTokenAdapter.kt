package org.p2p.wallet.home.ui.select

import android.view.ViewGroup
import org.p2p.wallet.common.ui.recycler.adapter.BaseSingleSelectionAdapter
import org.p2p.core.token.Token

class NewSelectTokenAdapter(
    preselectedItem: Token? = null,
    onItemClicked: (Token) -> Unit = {}
) : BaseSingleSelectionAdapter<Token, NewSelectTokenViewHolder>(preselectedItem, onItemClicked) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
        onItemClicked: (Token) -> Unit
    ): NewSelectTokenViewHolder = NewSelectTokenViewHolder(parent, onItemClicked = onItemClicked)

    override fun onBindViewHolder(
        holder: NewSelectTokenViewHolder,
        item: Token,
        selectedItem: Token?
    ) = holder.onBind(item, selectedItem)
}

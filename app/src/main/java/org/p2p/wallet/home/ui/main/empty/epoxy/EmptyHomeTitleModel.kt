package org.p2p.wallet.home.ui.main.empty.epoxy

import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemMainHeaderBinding
import org.p2p.wallet.home.model.EmptyHomeItem

class EmptyHomeTitleModel(
    val item: EmptyHomeItem.EmptyHomeTitleItem
) : ViewBindingKotlinModel<ItemMainHeaderBinding>(R.layout.item_main_header) {

    override fun ItemMainHeaderBinding.bind() {
        textViewHeader.text = item.title
    }
}

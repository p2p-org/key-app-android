package org.p2p.wallet.home.ui.main.adapter

import org.p2p.wallet.common.delegates.SmartDelegate
import org.p2p.wallet.databinding.ItemMainHeaderBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class HeaderDelegate() : SmartDelegate<String, ItemMainHeaderBinding>(
    { parent -> parent.inflateViewBinding(attachToRoot = false) }
) {

    override fun onBindViewHolder(
        holder: ViewHolder<ItemMainHeaderBinding>,
        data: String
    ) = with(holder.binding) {
        textViewHeader.text = data
    }
}

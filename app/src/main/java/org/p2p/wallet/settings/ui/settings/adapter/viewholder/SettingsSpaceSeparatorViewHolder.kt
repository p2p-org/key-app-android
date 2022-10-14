package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import org.p2p.wallet.databinding.ItemSettingsSpaceSeparatorBinding
import org.p2p.wallet.settings.model.SettingsItem.SettingsSpaceSeparatorItem

class SettingsSpaceSeparatorViewHolder(
    binding: ItemSettingsSpaceSeparatorBinding
) : NewSettingsViewHolder<ItemSettingsSpaceSeparatorBinding, SettingsSpaceSeparatorItem>(binding) {
    override fun ItemSettingsSpaceSeparatorBinding.bind(item: SettingsSpaceSeparatorItem) = Unit
}

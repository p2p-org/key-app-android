package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import org.p2p.wallet.databinding.ItemSettingsGroupTitleBinding
import org.p2p.wallet.settings.model.SettingsItem.SettingsGroupTitleItem

class SettingsGroupTitleViewHolder(
    binding: ItemSettingsGroupTitleBinding,
) : NewSettingsViewHolder<ItemSettingsGroupTitleBinding, SettingsGroupTitleItem>(binding) {
    override fun ItemSettingsGroupTitleBinding.bind(item: SettingsGroupTitleItem) {
        settingsGroupTitle.setText(item.groupTitleRes)
    }
}

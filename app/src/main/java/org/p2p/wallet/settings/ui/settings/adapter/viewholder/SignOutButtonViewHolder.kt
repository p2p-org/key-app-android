package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import org.p2p.wallet.databinding.ItemSettingsSignOutBinding
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.SignOutButtonItem
import org.p2p.wallet.settings.ui.settings.adapter.SettingsItemClickListener

class SignOutButtonViewHolder(
    binding: ItemSettingsSignOutBinding,
    private val settingsItemClickListener: SettingsItemClickListener
) : SettingsViewHolder<ItemSettingsSignOutBinding, SignOutButtonItem>(binding) {
    override fun ItemSettingsSignOutBinding.bind(item: SignOutButtonItem) {
        root.setOnClickListener { settingsItemClickListener.onSettingsItemClicked(item) }
    }
}

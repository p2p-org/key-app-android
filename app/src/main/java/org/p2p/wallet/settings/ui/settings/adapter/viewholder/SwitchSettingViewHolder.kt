package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import androidx.core.view.isVisible
import org.p2p.uikit.utils.getString
import org.p2p.wallet.databinding.ItemSettingsSwitchBinding
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.SwitchSettingsItem
import org.p2p.wallet.settings.ui.settings.adapter.SettingsItemClickListener

class SwitchSettingViewHolder(
    binding: ItemSettingsSwitchBinding,
    private val settingsItemClickListener: SettingsItemClickListener
) : NewSettingsViewHolder<ItemSettingsSwitchBinding, SwitchSettingsItem>(binding) {
    override fun ItemSettingsSwitchBinding.bind(item: SwitchSettingsItem) {
        imageViewSettingIcon.setImageResource(item.iconRes)
        textViewSettingName.text = getString(item.settingNameRes)

        switchChangeSettings.setOnCheckedChangeListener(null)
        switchChangeSettings.isChecked = item.isSwitched
        switchChangeSettings.setOnCheckedChangeListener { _, isChecked ->
            settingsItemClickListener.onSettingsItemClicked(item.copy(isSwitched = isChecked))
        }

        viewSeparator.isVisible = item.hasSeparator
    }
}

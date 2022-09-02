package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import androidx.core.view.isVisible
import org.p2p.uikit.utils.getString
import org.p2p.wallet.databinding.ItemSettingsSwitchBinding
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.SwitchSettingsItem

class SwitchSettingViewHolder(
    binding: ItemSettingsSwitchBinding,
    private val onSettingsClicked: (SettingsItem) -> Unit
) : NewSettingsViewHolder<ItemSettingsSwitchBinding, SwitchSettingsItem>(binding) {
    override fun ItemSettingsSwitchBinding.bind(item: SwitchSettingsItem) {
        imageViewSettingIcon.setImageResource(item.iconRes)
        textViewSettingName.text = getString(item.settingNameRes)

        switchChangeSettings.setOnCheckedChangeListener(null)
        switchChangeSettings.isChecked = item.isSwitched
        switchChangeSettings.setOnCheckedChangeListener { _, isChecked ->
            onSettingsClicked(item.copy(isSwitched = isChecked))
        }

        viewSeparator.isVisible = item.hasSeparator
    }
}

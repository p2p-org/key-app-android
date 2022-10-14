package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import androidx.core.view.isVisible
import org.p2p.uikit.utils.getString
import org.p2p.wallet.databinding.ItemSettingsSwitchBinding
import org.p2p.wallet.settings.model.SettingsItem
import org.p2p.wallet.settings.model.SettingsItem.SwitchSettingsItem

class SwitchSettingsViewHolder(
    private val binding: ItemSettingsSwitchBinding,
    private val onSettingsClicked: (SettingsItem) -> Unit
) : NewSettingsViewHolder<ItemSettingsSwitchBinding, SwitchSettingsItem>(binding) {

    override fun ItemSettingsSwitchBinding.bind(item: SwitchSettingsItem) {
        imageViewSettingIcon.setImageResource(item.iconRes)
        textViewSettingName.text = getString(item.nameRes)
        viewSeparator.isVisible = item.hasSeparator

        bindSwitch(item)
    }

    fun bindSwitch(item: SwitchSettingsItem) {
        binding.switchChangeSettings.setOnCheckedChangeListener(null)
        binding.switchChangeSettings.isChecked = item.isSwitched
        binding.switchChangeSettings.setOnCheckedChangeListener { _, isChecked ->
            onSettingsClicked(item.copy(isSwitched = isChecked))
        }
    }
}

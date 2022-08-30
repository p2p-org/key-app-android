package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import androidx.core.view.isVisible
import org.p2p.uikit.utils.getString
import org.p2p.wallet.databinding.ItemSettingsSwitchBinding
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.SwitchSettingItem
import org.p2p.wallet.settings.ui.settings.adapter.SettingsItemClickListener

class SwitchSettingViewHolder(
    binding: ItemSettingsSwitchBinding,
    private val settingsItemClickListener: SettingsItemClickListener
) : SettingsViewHolder<ItemSettingsSwitchBinding, SwitchSettingItem>(binding) {
    override fun ItemSettingsSwitchBinding.bind(item: SwitchSettingItem) {
        imageViewSettingIcon.setImageResource(item.iconRes)
        textViewSettingName.text = getString(item.settingNameRes)

        switchChangeSetting.setOnCheckedChangeListener(null)
        switchChangeSetting.isChecked = item.isSwitched
        switchChangeSetting.setOnCheckedChangeListener { _, isChecked ->
            settingsItemClickListener.onSettingsItemClicked(item.copy(isSwitched = isChecked))
        }

        viewSeparator.isVisible = item.hasSeparator
    }
}

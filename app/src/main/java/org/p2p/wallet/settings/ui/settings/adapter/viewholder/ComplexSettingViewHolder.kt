package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import androidx.core.view.isVisible
import org.p2p.uikit.utils.getString
import org.p2p.wallet.databinding.ItemSettingsComplexBinding
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.ComplexSettingItem
import org.p2p.wallet.settings.ui.settings.adapter.SettingsItemClickListener

class ComplexSettingViewHolder(
    binding: ItemSettingsComplexBinding,
    private val settingsItemClickListener: SettingsItemClickListener
) : SettingsViewHolder<ItemSettingsComplexBinding, ComplexSettingItem>(binding) {
    override fun ItemSettingsComplexBinding.bind(item: ComplexSettingItem) {
        imageViewSettingIcon.setImageResource(item.iconRes)
        textViewSettingName.text = getString(item.settingNameRes)
        item.additionalText?.also { textViewSettingValue.text = it }

        imageViewOpenSetting.setOnClickListener {
            settingsItemClickListener.onSettingsItemClicked(item)
        }

        viewSeparator.isVisible = item.hasSeparator
    }
}

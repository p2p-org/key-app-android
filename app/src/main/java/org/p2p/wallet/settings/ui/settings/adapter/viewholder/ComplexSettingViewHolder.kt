package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import androidx.core.view.isVisible
import org.p2p.uikit.utils.getString
import org.p2p.wallet.databinding.ItemSettingsComplexBinding
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.ComplexSettingsItem
import org.p2p.wallet.settings.ui.settings.adapter.SettingsItemClickListener

class ComplexSettingViewHolder(
    binding: ItemSettingsComplexBinding,
    private val settingsItemClickListener: SettingsItemClickListener
) : NewSettingsViewHolder<ItemSettingsComplexBinding, ComplexSettingsItem>(binding) {
    override fun ItemSettingsComplexBinding.bind(item: ComplexSettingsItem) {
        imageViewSettingIcon.setImageResource(item.iconRes)
        textViewSettingName.text = getString(item.settingNameRes)
        item.additionalText?.also { textViewSettingValue.text = it }

        imageViewOpenSetting.setOnClickListener {
            settingsItemClickListener.onSettingsItemClicked(item)
        }

        viewSeparator.isVisible = item.hasSeparator
    }
}

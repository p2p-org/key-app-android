package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import androidx.core.view.isVisible
import org.p2p.wallet.databinding.ItemSettingsTextValueBinding
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.TextSettingItem

class TextSettingViewHolder(
    binding: ItemSettingsTextValueBinding,
) : SettingsViewHolder<ItemSettingsTextValueBinding, TextSettingItem>(binding) {
    override fun ItemSettingsTextValueBinding.bind(item: TextSettingItem) {
        imageViewSettingIcon.setImageResource(item.iconRes)
        textViewSettingName.setText(item.settingNameRes)
        textViewSettingValue.text = item.textValue

        viewSeparator.isVisible = item.hasSeparator
    }
}

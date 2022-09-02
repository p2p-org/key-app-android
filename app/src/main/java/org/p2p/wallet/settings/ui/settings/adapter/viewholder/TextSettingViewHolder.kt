package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import androidx.core.view.isVisible
import org.p2p.wallet.databinding.ItemSettingsTextValueBinding
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem.TextSettingsItem

class TextSettingViewHolder(
    binding: ItemSettingsTextValueBinding,
) : NewSettingsViewHolder<ItemSettingsTextValueBinding, TextSettingsItem>(binding) {
    override fun ItemSettingsTextValueBinding.bind(item: TextSettingsItem) {
        imageViewSettingIcon.setImageResource(item.iconRes)
        textViewSettingName.setText(item.settingNameRes)
        textViewSettingValue.text = item.textValue

        viewSeparator.isVisible = item.hasSeparator
    }
}

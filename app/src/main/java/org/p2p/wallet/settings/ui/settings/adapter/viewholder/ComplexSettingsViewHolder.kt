package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import androidx.core.view.isVisible
import org.p2p.uikit.utils.getString
import org.p2p.wallet.databinding.ItemSettingsComplexBinding
import org.p2p.wallet.settings.model.SettingsItem
import org.p2p.wallet.settings.model.SettingsItem.ComplexSettingsItem

class ComplexSettingsViewHolder(
    binding: ItemSettingsComplexBinding,
    private val onSettingsClicked: (SettingsItem) -> Unit
) : NewSettingsViewHolder<ItemSettingsComplexBinding, ComplexSettingsItem>(binding) {
    override fun ItemSettingsComplexBinding.bind(item: ComplexSettingsItem) {
        imageViewBadge.isVisible = item.isBadgeVisible
        imageViewSettingsIcon.setImageResource(item.iconRes)
        textViewSettingName.text = getString(item.nameRes)
        item.additionalText?.also { textViewSettingValue.text = it }

        itemView.setOnClickListener {
            onSettingsClicked(item)
        }

        viewSeparator.isVisible = item.hasSeparator
    }
}

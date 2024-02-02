package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import org.p2p.wallet.databinding.ItemReferralBannerBinding
import org.p2p.wallet.settings.model.SettingsItem

class ReferralBannerSettingsViewHolder(
    private val binding: ItemReferralBannerBinding,
    private val onClickShareLink: () -> Unit,
    private val onClickOpenDetails: () -> Unit
) : NewSettingsViewHolder<ItemReferralBannerBinding, SettingsItem.ReferralBannerSettingsItem>(binding) {

    override fun ItemReferralBannerBinding.bind(item: SettingsItem.ReferralBannerSettingsItem) {
        binding.buttonShare.setOnClickListener {
            onClickShareLink()
        }
        binding.buttonOpenDetails.setOnClickListener {
            onClickOpenDetails()
        }
    }
}

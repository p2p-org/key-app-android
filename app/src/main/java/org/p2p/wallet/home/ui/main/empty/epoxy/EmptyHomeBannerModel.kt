package org.p2p.wallet.home.ui.main.empty.epoxy

import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemBigBannerBinding
import org.p2p.wallet.home.model.HomeBannerItem

// data class is reqiured due to auto diff
data class EmptyHomeBannerModel(
    val item: HomeBannerItem,
    val onBannerClicked: (buttonId: Int) -> Unit
) : ViewBindingKotlinModel<ItemBigBannerBinding>(R.layout.item_big_banner) {
    override fun ItemBigBannerBinding.bind() {
        textViewBannerTitle.setText(item.titleTextId)
        textViewBannerSubtitle.setText(item.subtitleTextId)

        imageViewBanner.setImageResource(item.drawableRes)

        buttonBanner.apply {
            setText(item.buttonTextId)
            setOnClickListener { onBannerClicked(item.id) }
        }
    }
}


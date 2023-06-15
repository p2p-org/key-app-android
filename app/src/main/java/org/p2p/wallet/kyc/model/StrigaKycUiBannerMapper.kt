package org.p2p.wallet.kyc.model

import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.model.HomeScreenBanner

class StrigaKycUiBannerMapper {

    fun mapToBigBanner(status: StrigaKycStatusBanner): HomeBannerItem {
        return status.toHomeBannerItem()
    }

    fun mapToBanner(status: StrigaKycStatusBanner): HomeScreenBanner {
        return StrigaBanner(status)
    }

    fun onBannerClicked(bannerTitleId: Int): StrigaKycStatusBanner {
        return StrigaKycStatusBanner.values().first { it.bannerTitleResId == bannerTitleId }
    }
}

private fun StrigaKycStatusBanner.toHomeBannerItem(): HomeBannerItem {
    return HomeBannerItem(
        titleTextId = bigBannerTitleResId,
        subtitleTextId = bigBannerMessageResId,
        buttonTextId = actionTitleResId,
        drawableRes = placeholderResId,
        backgroundColorRes = backgroundTint
    )
}

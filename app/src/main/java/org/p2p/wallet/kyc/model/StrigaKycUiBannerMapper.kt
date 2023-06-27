package org.p2p.wallet.kyc.model

import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.model.HomeScreenBanner

class StrigaKycUiBannerMapper {

    fun mapToHomeBigBanner(status: StrigaKycStatusBanner): HomeBannerItem = status.toHomeBannerItem()

    fun mapToBanner(status: StrigaKycStatusBanner): HomeScreenBanner = StrigaBanner(status)

    fun getKycStatusBannerFromTitle(bannerTitleId: Int): StrigaKycStatusBanner? {
        return StrigaKycStatusBanner.values().firstOrNull {
            it.bannerTitleResId == bannerTitleId || it.bigBannerTitleResId == bannerTitleId
        }
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

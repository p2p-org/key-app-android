package org.p2p.wallet.kyc.model

import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.model.HomeScreenBanner

class StrigaKycUiBannerMapper {

    fun mapToHomeBigBanner(
        status: StrigaKycStatusBanner,
        isLoading: Boolean
    ): HomeBannerItem = status.toHomeBannerItem(isLoading)

    fun mapToBanner(
        isLoading: Boolean,
        status: StrigaKycStatusBanner
    ): HomeScreenBanner = StrigaBanner(isLoading, status)

    fun getKycStatusBannerFromTitle(bannerTitleId: Int): StrigaKycStatusBanner? {
        return StrigaKycStatusBanner.values().firstOrNull {
            it.bannerTitleResId == bannerTitleId || it.bigBannerTitleResId == bannerTitleId
        }
    }
}

private fun StrigaKycStatusBanner.toHomeBannerItem(isLoading: Boolean): HomeBannerItem {
    return HomeBannerItem(
        titleTextId = bigBannerTitleResId,
        subtitleTextId = bigBannerMessageResId,
        buttonTextId = actionTitleResId,
        drawableRes = placeholderResId,
        backgroundColorRes = backgroundTint,
        isLoading = isLoading
    )
}

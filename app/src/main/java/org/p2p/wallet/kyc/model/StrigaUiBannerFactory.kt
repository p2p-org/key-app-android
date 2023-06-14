package org.p2p.wallet.kyc.model

import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.model.HomeScreenBanner

class StrigaKycUiBannerMapper {

    fun mapToBigBanner(status: StrigaKycSignUpStatus): HomeBannerItem {
        return status.toHomeBannerItem()
    }

    fun mapToBanner(status: StrigaKycSignUpStatus): HomeScreenBanner {
        return StrigaKycBanner(status)
    }

    fun onBannerClicked(bannerTitleId: Int): StrigaKycSignUpStatus {
        return StrigaKycSignUpStatus.values().first { it.bannerTitleResId == bannerTitleId }
    }
}

private fun StrigaKycSignUpStatus.toHomeBannerItem(): HomeBannerItem {
    return HomeBannerItem(
        titleTextId = bigBannerTitleResId,
        subtitleTextId = bigBannerMessageResId,
        buttonTextId = actionTitleResId,
        drawableRes = placeholderResId,
        backgroundColorRes = backgroundTint
    )
}

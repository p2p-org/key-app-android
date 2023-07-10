package org.p2p.wallet.kyc.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.R

/**
 * @param actionTitleResId - if null button will be hidden
 */
enum class StrigaKycStatusBanner(
    @StringRes val bannerTitleResId: Int,
    @StringRes val bigBannerTitleResId: Int,
    @StringRes val bannerMessageResId: Int,
    @StringRes val bigBannerMessageResId: Int,
    @StringRes val actionTitleResId: Int?,
    @DrawableRes val placeholderResId: Int,
    @ColorRes val backgroundTint: Int,
    val bannerId: Int
) {

    IDENTIFY(
        bannerTitleResId = R.string.striga_kyc_signup_banner_identify_title,
        bigBannerTitleResId = R.string.striga_kyc_signup_big_banner_identify_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_identify_subtitle,
        bigBannerMessageResId = R.string.striga_kyc_signup_banner_identify_subtitle,
        actionTitleResId = R.string.striga_kyc_signup_banner_identify_action,
        placeholderResId = R.drawable.ic_striga_kyc_identify,
        backgroundTint = R.color.light_sea,
        bannerId = 1
    ),
    PENDING(
        bannerTitleResId = R.string.striga_kyc_signup_banner_pending_title,
        bigBannerTitleResId = R.string.striga_kyc_signup_big_banner_pending_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_pending_message,
        bigBannerMessageResId = R.string.striga_kyc_signup_big_banner_pending_message,
        actionTitleResId = null,
        placeholderResId = R.drawable.ic_clock,
        backgroundTint = R.color.light_sea,
        bannerId = 2
    ),
    VERIFICATION_DONE(
        bannerTitleResId = R.string.striga_kyc_signup_banner_finish_title,
        bigBannerTitleResId = R.string.striga_kyc_signup_big_banner_finish_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_finish_message,
        bigBannerMessageResId = R.string.striga_kyc_signup_big_banner_finish_message,
        actionTitleResId = R.string.striga_kyc_signup_banner_finish_action,
        placeholderResId = R.drawable.ic_striga_kyc_approved,
        backgroundTint = R.color.light_grass,
        bannerId = 3
    ),
    ACTION_REQUIRED(
        bannerTitleResId = R.string.striga_kyc_signup_banner_action_required_title,
        bigBannerTitleResId = R.string.striga_kyc_signup_banner_action_required_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_action_required_message,
        bigBannerMessageResId = R.string.striga_kyc_signup_banner_action_required_message,
        actionTitleResId = R.string.striga_kyc_signup_banner_action_required_action,
        placeholderResId = R.drawable.ic_shield_eye,
        backgroundTint = R.color.light_sun,
        bannerId = 4
    ),
    REJECTED(
        bannerTitleResId = R.string.striga_kyc_signup_banner_rejected_title,
        bigBannerTitleResId = R.string.striga_kyc_signup_banner_rejected_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_rejected_message,
        bigBannerMessageResId = R.string.striga_kyc_signup_banner_rejected_message,
        actionTitleResId = R.string.striga_kyc_signup_banner_rejected_action,
        placeholderResId = R.drawable.ic_low_speed,
        backgroundTint = R.color.light_rose,
        bannerId = 5
    )
}

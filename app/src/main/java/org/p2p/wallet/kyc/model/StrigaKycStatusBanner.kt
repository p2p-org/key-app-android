package org.p2p.wallet.kyc.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.view.View
import org.p2p.wallet.R

/**
 * @param actionTitleResId - if [android.view.View.NO_ID] button will be hidden
 */
enum class StrigaKycStatusBanner(
    @StringRes val bannerTitleResId: Int,
    @StringRes val bigBannerTitleResId: Int,
    @StringRes val bannerMessageResId: Int,
    @StringRes val bigBannerMessageResId: Int,
    @StringRes val actionTitleResId: Int,
    @DrawableRes val placeholderResId: Int,
    @ColorRes val backgroundTint: Int,
) {

    IDENTIFY(
        bannerTitleResId = R.string.striga_kyc_signup_banner_identify_title,
        bigBannerTitleResId = R.string.striga_kyc_signup_big_banner_identify_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_identify_subtitle,
        bigBannerMessageResId = R.string.striga_kyc_signup_banner_identify_subtitle,
        actionTitleResId = R.string.striga_kyc_signup_banner_identify_action,
        placeholderResId = R.drawable.onboarding_slide_2,
        backgroundTint = R.color.light_sea
    ),
    PENDING(
        bannerTitleResId = R.string.striga_kyc_signup_banner_pending_title,
        bigBannerTitleResId = R.string.striga_kyc_signup_big_banner_pending_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_pending_message,
        bigBannerMessageResId = R.string.striga_kyc_signup_big_banner_pending_message,
        actionTitleResId = View.NO_ID,
        placeholderResId = R.drawable.ic_clock,
        backgroundTint = R.color.light_sea
    ),
    VERIFICATION_DONE(
        bannerTitleResId = R.string.striga_kyc_signup_banner_finish_title,
        bigBannerTitleResId = R.string.striga_kyc_signup_big_banner_finish_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_finish_message,
        bigBannerMessageResId = R.string.striga_kyc_signup_big_banner_finish_message,
        actionTitleResId = R.string.striga_kyc_signup_banner_finish_action,
        placeholderResId = R.drawable.ic_send_no_token_placeholder,
        backgroundTint = R.color.light_grass
    ),
    ACTION_REQUIRED(
        bannerTitleResId = R.string.striga_kyc_signup_banner_action_required_title,
        bigBannerTitleResId = R.string.striga_kyc_signup_banner_action_required_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_action_required_message,
        bigBannerMessageResId = R.string.striga_kyc_signup_banner_action_required_message,
        actionTitleResId = R.string.striga_kyc_signup_banner_action_required_action,
        placeholderResId = R.drawable.ic_shield_eye,
        backgroundTint = R.color.light_sun
    ),
    REJECTED(
        bannerTitleResId = R.string.striga_kyc_signup_banner_rejected_title,
        bigBannerTitleResId = R.string.striga_kyc_signup_banner_rejected_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_rejected_message,
        bigBannerMessageResId = R.string.striga_kyc_signup_banner_rejected_message,
        actionTitleResId = R.string.striga_kyc_signup_banner_rejected_action,
        placeholderResId = R.drawable.ic_low_speed,
        backgroundTint = R.color.light_rose
    )
}

package org.p2p.wallet.kyc.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.R

enum class StrigaKycSignUpStatus(
    @StringRes val bannerTitleResId: Int,
    @StringRes val bannerMessageResId: Int,
    @StringRes val actionTitleResId: Int,
    @DrawableRes val placeholderResId: Int,
    @ColorRes val backgroundTint: Int,
    val isCloseButtonVisible: Boolean = true
) {

    IDENTIFY(
        bannerTitleResId = R.string.striga_kyc_signup_banner_identify_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_identify_subtitle,
        actionTitleResId = R.string.striga_kyc_signup_banner_identify_action,
        placeholderResId = R.drawable.onboarding_slide_2,
        backgroundTint = R.color.light_sea
    ),
    PENDING(
        bannerTitleResId = R.string.striga_kyc_signup_banner_pending_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_pending_message,
        actionTitleResId = R.string.striga_kyc_signup_banner_pending_action,
        placeholderResId = R.drawable.ic_clock,
        backgroundTint = R.color.light_sea
    ),
    VERIFICATION_DONE(
        bannerTitleResId = R.string.striga_kyc_signup_banner_finish_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_finish_message,
        actionTitleResId = R.string.striga_kyc_signup_banner_finish_action,
        placeholderResId = R.drawable.ic_send_no_token_placeholder,
        backgroundTint = R.color.light_grass,
        isCloseButtonVisible = false
    ),
    ACTION_REQUIRED(
        bannerTitleResId = R.string.striga_kyc_signup_banner_action_required_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_action_required_message,
        actionTitleResId = R.string.striga_kyc_signup_banner_action_required_action,
        placeholderResId = R.drawable.ic_shield_eye,
        backgroundTint = R.color.light_sun
    ),
    REJECTED(
        bannerTitleResId = R.string.striga_kyc_signup_banner_action_required_title,
        bannerMessageResId = R.string.striga_kyc_signup_banner_action_required_message,
        actionTitleResId = R.string.striga_kyc_signup_banner_action_required_action,
        placeholderResId = R.drawable.ic_low_speed,
        backgroundTint = R.color.light_rose
    )
}

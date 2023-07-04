package org.p2p.wallet.striga.sms.error

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.R

@Parcelize
sealed class StrigaSmsErrorViewType(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @ColorRes val helpButtonColorRes: Int
) : Parcelable {

    @Parcelize
    class NumberAlreadyUsed : StrigaSmsErrorViewType(
        imageRes = R.drawable.ic_hand_with_square,
        titleRes = R.string.striga_signup_error_number_already_used_title,
        subtitleRes = R.string.striga_signup_error_number_already_used_subtitle,
        helpButtonColorRes = R.color.lime
    )

    @Parcelize
    class ExceededResendAttempts : StrigaSmsErrorViewType(
        imageRes = R.drawable.ic_timer_error_raster,
        titleRes = R.string.striga_sms_error_exceeded_resend_attempts_title,
        subtitleRes = R.string.striga_sms_error_exceeded_resend_attempts_subtitle,
        helpButtonColorRes = R.color.snow
    )

    @Parcelize
    class ExceededConfirmationAttempts : StrigaSmsErrorViewType(
        imageRes = R.drawable.ic_timer_error_raster,
        titleRes = R.string.striga_sms_error_exceeded_confirmation_attempts_title,
        subtitleRes = R.string.striga_sms_error_exceeded_confirmation_attempts_subtitle,
        helpButtonColorRes = R.color.snow
    )
}

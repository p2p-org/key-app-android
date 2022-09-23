package org.p2p.wallet.auth.ui.generalerror.timer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class GeneralErrorTimerScreenError : Parcelable {
    BLOCK_PHONE_NUMBER_ENTER,
    BLOCK_SMS_RETRY_BUTTON_TRIES_EXCEEDED,
    BLOCK_SMS_TOO_MANY_WRONG_ATTEMPTS
}

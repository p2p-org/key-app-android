package org.p2p.wallet.auth.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface GatewayHandledState : Parcelable {

    @Parcelize
    data class CriticalError(val errorCode: Int) : GatewayHandledState

    @Parcelize
    object IncorrectOtpCodeError : GatewayHandledState

    @Parcelize
    data class ToastError(val message: String) : GatewayHandledState

    @Parcelize
    data class TimerBlockError(
        val cooldownTtl: Long
    ) : GatewayHandledState

    @Parcelize
    data class TitleSubtitleError(
        val title: String,
        val subtitle: String,
        val email: String? = null,
        val googleButton: GoogleButton? = null,
        val primaryFirstButton: PrimaryFirstButton? = null,
        val secondaryFirstButton: SecondaryFirstButton? = null
    ) : GatewayHandledState
}

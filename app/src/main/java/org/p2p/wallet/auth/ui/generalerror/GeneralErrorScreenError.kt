package org.p2p.wallet.auth.ui.generalerror

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class GeneralErrorScreenError : Parcelable {
    @Parcelize
    class CriticalError(val errorCode: Int) : GeneralErrorScreenError()
}

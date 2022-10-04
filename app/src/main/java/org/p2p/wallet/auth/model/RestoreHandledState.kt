package org.p2p.wallet.auth.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

abstract class RestoreHandledState
class RestoreSuccessState : RestoreHandledState()

abstract class RestoreFailureState(
    open val googleButton: GoogleButton? = null,
    open val primaryFirstButton: PrimaryFirstButton? = null,
    open val secondaryFirstButton: SecondaryFirstButton? = null
) : RestoreHandledState(), Parcelable {

    @Parcelize
    data class TitleSubtitleError(
        val title: String,
        val subtitle: String,
        val email: String? = null,
        val imageViewResId: Int? = null,
        override val googleButton: GoogleButton? = null,
        override val primaryFirstButton: PrimaryFirstButton? = null,
        override val secondaryFirstButton: SecondaryFirstButton? = null
    ) : RestoreFailureState(
        googleButton = GoogleButton(),
        primaryFirstButton = PrimaryFirstButton(),
        secondaryFirstButton = SecondaryFirstButton()
    )

    @Parcelize
    data class ToastError(val message: String) : RestoreFailureState()

    @Parcelize
    data class LogError(val message: String) : RestoreFailureState()
}

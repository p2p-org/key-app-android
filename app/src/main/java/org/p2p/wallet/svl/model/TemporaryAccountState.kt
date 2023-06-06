package org.p2p.wallet.svl.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.Token

sealed interface TemporaryAccountState : Parcelable {

    @Parcelize
    data class Active(
        val account: TemporaryAccount,
        val token: Token.Active
    ) : TemporaryAccountState

    @Parcelize
    object EmptyBalance : TemporaryAccountState

    @Parcelize
    object ParsingFailed : TemporaryAccountState

    @Parcelize
    object BrokenLink : TemporaryAccountState
}

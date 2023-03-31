package org.p2p.wallet.svl.model

import org.p2p.core.token.Token
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.newsend.model.TemporaryAccount

sealed interface SendViaLinkClaimingState {
    data class ReadyToClaim(
        val temporaryAccount: TemporaryAccount,
        val token: Token.Active
    ) : SendViaLinkClaimingState

    object ClaimingInProcess : SendViaLinkClaimingState
    data class ClaimSuccess(val successMessage: TextViewCellModel) : SendViaLinkClaimingState
    data class ClaimFailed(val cause: Throwable) : SendViaLinkClaimingState
    object ParsingFailed : SendViaLinkClaimingState
}

package org.p2p.wallet.svl.model

import org.p2p.wallet.newsend.model.TemporaryAccount

sealed interface SendViaLinkClaimingState {
    data class ReadyToClaim(
        val temporaryAccount: TemporaryAccount,
        val amountInTokens: String,
        val tokenSymbol: String
    ) : SendViaLinkClaimingState

    object ClaimingInProcess : SendViaLinkClaimingState
    data class ClaimSuccess(val tokenAmount: String, val tokenSymbol: String) : SendViaLinkClaimingState
    data class ClaimFailed(val cause: Throwable) : SendViaLinkClaimingState
    object ParsingFailed : SendViaLinkClaimingState
}

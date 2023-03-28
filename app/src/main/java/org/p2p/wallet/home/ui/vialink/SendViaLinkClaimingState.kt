package org.p2p.wallet.home.ui.vialink

sealed interface SendViaLinkClaimingState {
    object ReadyToClaim : SendViaLinkClaimingState
    object ClaimingInProcess : SendViaLinkClaimingState
    data class ClaimSuccess(val tokenAmount: String, val tokenSymbol: String) : SendViaLinkClaimingState
    data class ClaimFailed(val cause: Throwable) : SendViaLinkClaimingState
}

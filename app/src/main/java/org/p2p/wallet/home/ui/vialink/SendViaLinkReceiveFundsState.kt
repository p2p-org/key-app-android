package org.p2p.wallet.home.ui.vialink

sealed interface SendViaLinkReceiveFundsState {
    object ReadyToClaim : SendViaLinkReceiveFundsState
    object ClaimingInProcess : SendViaLinkReceiveFundsState
    object ClaimSuccess : SendViaLinkReceiveFundsState
    data class ClaimFailed(val cause: Throwable) : SendViaLinkReceiveFundsState
}

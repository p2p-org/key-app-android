package org.p2p.wallet.send.model

sealed interface FeePayerState {
    object UpdateFeePayer : FeePayerState
    object SwitchToSol : FeePayerState
}

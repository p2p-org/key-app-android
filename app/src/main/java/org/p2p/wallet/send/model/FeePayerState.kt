package org.p2p.wallet.send.model

import java.math.BigInteger

sealed interface FeePayerState {
    object UpdateFeePayer : FeePayerState
    object SwitchToSol : FeePayerState
    class ReduceInputAmount(val maxAllowedAmount: BigInteger) : FeePayerState
}

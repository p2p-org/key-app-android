package org.p2p.wallet.send.model

import java.math.BigInteger
import org.p2p.core.token.Token

sealed interface FeePayerState {
    object SwitchToSol : FeePayerState
    object KeepSame : FeePayerState
    class SwitchToSpl(val tokenToSwitch: Token.Active) : FeePayerState
    class ReduceInputAmount(val maxAllowedAmount: BigInteger) : FeePayerState
}

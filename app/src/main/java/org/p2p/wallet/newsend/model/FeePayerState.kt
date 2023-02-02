package org.p2p.wallet.newsend.model

import org.p2p.core.token.Token
import java.math.BigInteger

sealed interface FeePayerState {
    object SwitchToSol : FeePayerState
    class SwitchToSpl(val tokenToSwitch: Token.Active) : FeePayerState
    class ReduceInputAmount(val maxAllowedAmount: BigInteger) : FeePayerState
}

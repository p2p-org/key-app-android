package org.p2p.wallet.newsend.model

import java.math.BigInteger
import org.p2p.core.token.Token

sealed interface FeePayerState {
    object Idle : FeePayerState
    object SwitchToSol : FeePayerState
    class SwitchToSpl(val tokenToSwitch: Token.Active) : FeePayerState
    class ReduceInputAmount(
        val fee: SendSolanaFee,
        val sourceToken: Token.Active,
        val maxAllowedAmount: BigInteger
    ) : FeePayerState
}

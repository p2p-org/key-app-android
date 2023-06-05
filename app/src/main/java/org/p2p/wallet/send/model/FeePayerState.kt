package org.p2p.wallet.send.model

import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee

sealed interface FeePayerState {
    data class FeePayerFound(val feePayerToken: Token.Active, val fee: FeeRelayerFee) : FeePayerState

    object NotApplicable : FeePayerState

    fun isValid(): Boolean = this is FeePayerFound

//    object CalculationError : FeePayerState
//
//    object InsufficientFunds : FeePayerState
//
//    object ExceededFee : FeePayerState
//
//    object LowMinBalance : FeePayerState
//
//    object InvalidAmountForRecipient : FeePayerState
//
//    object InvalidAmountForSender : FeePayerState
}

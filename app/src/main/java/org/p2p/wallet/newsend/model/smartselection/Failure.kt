package org.p2p.wallet.newsend.model.smartselection

import org.p2p.core.token.Token

sealed interface FeePayerFailureReason {
    data class CalculationError(val e: Throwable) : FeePayerFailureReason

    data class InputExceeded(val sourceToken: Token.Active) : FeePayerFailureReason

    object ExceededFee : FeePayerFailureReason

    object LowMinBalanceIgnored : FeePayerFailureReason

    data class InvalidAmountForRecipient(val minRequiredSolBalance: String) : FeePayerFailureReason

    data class InvalidAmountForSender(val maxSolAmountAllowed: String) : FeePayerFailureReason
}

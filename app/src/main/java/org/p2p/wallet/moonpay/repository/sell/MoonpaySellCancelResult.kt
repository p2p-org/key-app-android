package org.p2p.wallet.moonpay.repository.sell

sealed interface MoonpaySellCancelResult {
    object TransactionCancelled : MoonpaySellCancelResult
    data class CancelFailed(val cause: Throwable) : MoonpaySellCancelResult
}

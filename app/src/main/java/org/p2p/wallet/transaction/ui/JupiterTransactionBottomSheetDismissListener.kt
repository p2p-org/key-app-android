package org.p2p.wallet.transaction.ui

sealed interface JupiterTransactionDismissResult {
    object TransactionInProgress : JupiterTransactionDismissResult
    object TransactionSuccess : JupiterTransactionDismissResult
    data class SlippageChangeNeeded(val newSlippageValue: Double) : JupiterTransactionDismissResult
    object ManualSlippageChangeNeeded : JupiterTransactionDismissResult
    object TrySwapAgain : JupiterTransactionDismissResult
}

fun interface JupiterTransactionBottomSheetDismissListener {
    fun onBottomSheetDismissed(result: JupiterTransactionDismissResult)
}

package org.p2p.wallet.transaction.ui

import org.p2p.wallet.swap.model.Slippage

sealed interface JupiterTransactionDismissResult {
    object TransactionInProgress : JupiterTransactionDismissResult
    object TransactionSuccess : JupiterTransactionDismissResult
    data class SlippageChangeNeeded(val newSlippageValue: Slippage) : JupiterTransactionDismissResult
    object ManualSlippageChangeNeeded : JupiterTransactionDismissResult
    object TrySwapAgain : JupiterTransactionDismissResult
}

fun interface JupiterTransactionBottomSheetDismissListener {
    fun onBottomSheetDismissed(result: JupiterTransactionDismissResult)
}

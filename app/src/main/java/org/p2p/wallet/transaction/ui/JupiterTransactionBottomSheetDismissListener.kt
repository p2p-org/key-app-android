package org.p2p.wallet.transaction.ui

enum class JupiterTransactionDismissResult {
    IN_PROGRESS, SUCCESS, LOW_SLIPPAGE_ERROR, UNKNOWN_ERROR
}

fun interface JupiterTransactionBottomSheetDismissListener {
    fun onBottomSheetDismissed(result: JupiterTransactionDismissResult)
}

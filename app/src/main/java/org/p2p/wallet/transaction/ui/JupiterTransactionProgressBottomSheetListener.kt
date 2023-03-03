package org.p2p.wallet.transaction.ui

interface JupiterTransactionProgressBottomSheetListener {
    fun onSwapTryAgainClicked()
    fun onSwapIncreaseSlippageClicked()
    fun onBottomSheetDismissed(isTransactionSucceed: Boolean)
}

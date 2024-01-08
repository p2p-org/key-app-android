package org.p2p.wallet.jupiter.analytics

import org.p2p.core.analytics.Analytics

private const val SWAP_TRANSACTION_PROGRESS_SCREEN = "Swap_Transaction_Progress_Screen"
private const val SWAP_TRANSACTION_PROGRESS_SCREEN_DONE = "Swap_Transaction_Progress_Screen_Done"
private const val SWAP_ERROR_DEFAULT = "Swap_Error_Default"
private const val SWAP_ERROR_SLIPPAGE = "Swap_Error_Slippage"

class JupiterSwapTransactionDetailsAnalytics(private val tracker: Analytics) {
    fun logTransactionProgressOpened() {
        tracker.logEvent(SWAP_TRANSACTION_PROGRESS_SCREEN)
    }

    fun logTransactionDoneClicked() {
        tracker.logEvent(SWAP_TRANSACTION_PROGRESS_SCREEN_DONE)
    }

    fun logSwapErrorUnknown(isInternetError: Boolean) {
        tracker.logEvent(
            event = SWAP_ERROR_DEFAULT,
            params = mapOf("Is_Blockchain_Related" to !isInternetError)
        )
    }

    fun logSwapErrorSlippage() {
        tracker.logEvent(SWAP_ERROR_SLIPPAGE)
    }
}

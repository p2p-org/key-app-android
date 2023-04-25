package org.p2p.wallet.history.ui.historylist

import org.p2p.wallet.jupiter.model.SwapOpenedFrom

interface HistoryListViewClickListener {
    fun onTransactionClicked(transactionId: String)
    fun onSellTransactionClicked(transactionId: String)
    fun onSwapBannerClicked(
        sourceTokenMint: String,
        destinationTokenMint: String,
        sourceSymbol: String,
        destinationSymbol: String,
        openedFrom: SwapOpenedFrom
    )

    fun onUserSendLinksClicked()
}

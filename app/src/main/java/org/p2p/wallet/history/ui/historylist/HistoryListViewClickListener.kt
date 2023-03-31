package org.p2p.wallet.history.ui.historylist

interface HistoryListViewClickListener {
    fun onTransactionClicked(transactionId: String)
    fun onSellTransactionClicked(transactionId: String)
    fun onUserSendLinksClicked()
}

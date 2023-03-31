package org.p2p.wallet.history.ui.historylist

import org.p2p.core.utils.Constants
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

interface HistoryListViewContract {
    sealed class HistoryListViewType(val mintAddress: Base58String) {
        object AllHistory : HistoryListViewType(Constants.WRAPPED_SOL_MINT.toBase58Instance())
        class HistoryForToken(mintAddress: Base58String) : HistoryListViewType(mintAddress)
    }

    interface View : MvpView {
        interface HistoryListViewClickListener {
            fun onTransactionClicked(transactionId: String)
            fun onSellTransactionClicked(transactionId: String)
            fun onUserSendLinksClicked()
        }

        fun showPagingState(state: PagingState)
        fun showRefreshing(isRefreshing: Boolean)
        fun scrollToTop()
        fun showHistory(history: List<HistoryItem>)
        fun onTransactionClicked(transactionId: String)

        fun onSellTransactionClicked(transactionId: String)
        fun onUserSendLinksClicked()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory(historyType: HistoryListViewType)
        fun refreshHistory(historyType: HistoryListViewType)
        fun loadNextHistoryPage(historyType: HistoryListViewType)
        fun attach(historyType: HistoryListViewType)

        fun onItemClicked(historyItem: HistoryItem)
    }
}

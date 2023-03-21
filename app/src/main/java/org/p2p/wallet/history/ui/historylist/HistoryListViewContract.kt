package org.p2p.wallet.history.ui.historylist

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.ui.model.HistoryItem

interface HistoryListViewContract {
    interface View : MvpView {
        fun showPagingState(state: PagingState)
        fun showRefreshing(isRefreshing: Boolean)
        fun scrollToTop()
        fun showHistory(history: List<HistoryItem>)
        fun onTransactionClicked(transactionId: String)

        fun onSellTransactionClicked(transactionId: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory(mintAddress: String)
        fun refreshHistory(mintAddress: String)
        fun loadNextHistoryPage(mintAddress: String)
        fun attach(mintAddress: String)

        fun onItemClicked(historyItem: HistoryItem)
    }
}

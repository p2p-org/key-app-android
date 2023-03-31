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

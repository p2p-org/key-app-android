package org.p2p.wallet.history.ui.history

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.model.HistoryTransaction

interface HistoryContract {
    interface View : MvpView {
        fun showPagingState(state: PagingState)
        fun showHistory(items: List<HistoryTransaction>)
        fun openTransactionDetailsScreen(transaction: HistoryTransaction)
        fun showRefreshing(isRefreshing: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory()
        fun refreshHistory()
        fun loadNextHistoryPage()
        fun onItemClicked(transaction: HistoryTransaction)
    }
}

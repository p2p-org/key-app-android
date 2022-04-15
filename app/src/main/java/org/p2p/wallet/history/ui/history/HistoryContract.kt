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
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory(isRefresh: Boolean = false)
        fun onItemClicked(transaction: HistoryTransaction)
    }
}

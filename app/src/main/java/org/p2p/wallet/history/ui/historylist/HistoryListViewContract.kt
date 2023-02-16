package org.p2p.wallet.history.ui.historylist

import androidx.lifecycle.DefaultLifecycleObserver
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails

interface HistoryListViewContract {
    interface View : MvpView {
        fun showPagingState(state: PagingState)
        fun showRefreshing(isRefreshing: Boolean)
        fun scrollToTop()
        fun showHistory(history: List<HistoryItem>)

        fun onTransactionClicked(transaction: HistoryTransaction)
        fun onSellTransactionClicked(sellTransactionDetails: SellTransactionViewDetails)
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
        fun loadHistory()
        fun refreshHistory()
        fun loadNextHistoryPage()

        fun onItemClicked(historyItem: HistoryItem)
    }
}

package org.p2p.wallet.history.ui.history

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.moonpay.model.SellTransaction

interface HistoryContract {
    interface View : MvpView {
        fun showPagingState(state: PagingState)
        fun openTransactionDetailsScreen(transaction: HistoryTransaction)
        fun showRefreshing(isRefreshing: Boolean)
        fun scrollToTop()
        fun showHistory(
            blockChainTransactions: List<HistoryTransaction>,
            sellTransactions: List<SellTransaction>
        )
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory()
        fun refreshHistory()
        fun loadNextHistoryPage()
        fun onItemClicked(transaction: HistoryTransaction)
        fun onResume()
    }
}

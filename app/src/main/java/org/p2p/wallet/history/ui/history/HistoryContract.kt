package org.p2p.wallet.history.ui.history

import androidx.lifecycle.DefaultLifecycleObserver
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails

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

        fun openSellTransactionDetails(sellTransaction: SellTransactionViewDetails)
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
        fun loadHistory()
        fun refreshHistory()
        fun loadNextHistoryPage()
        fun onItemClicked(transaction: HistoryTransaction)
        fun onSellTransactionClicked(sellTransaction: SellTransactionViewDetails)
    }
}

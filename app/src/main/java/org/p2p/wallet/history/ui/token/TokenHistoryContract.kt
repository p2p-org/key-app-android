package org.p2p.wallet.history.ui.token

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction

interface TokenHistoryContract {

    interface View : MvpView {
        fun showPagingState(newState: PagingState)
        fun showRefreshing(isRefreshing: Boolean)
        fun showError(@StringRes resId: Int, argument: String)
        fun openTransactionDetailsScreen(transaction: HistoryTransaction)
        fun scrollToTop()
        fun hideBuyActionButton()
        fun showHistory(
            transactions: List<HistoryTransaction>,
            moonpayTransactionItem: List<HistoryItem.MoonpayTransactionItem>
        )
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory()
        fun onItemClicked(transaction: HistoryTransaction)
        fun closeAccount()
        fun loadNextHistoryPage()
        fun retryLoad()
    }
}

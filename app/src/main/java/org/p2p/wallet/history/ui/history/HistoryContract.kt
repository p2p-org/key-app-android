package org.p2p.wallet.history.ui.history

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.ActionButtonsView
import org.p2p.wallet.history.model.HistoryTransaction

interface HistoryContract {

    interface View : MvpView {
        fun showHistory(transactions: List<HistoryTransaction>)
        fun showActions(items: List<ActionButtonsView.ActionButton>)
        fun showPagingState(newState: PagingState)
        fun showRefreshing(isRefreshing: Boolean)
        fun showError(@StringRes resId: Int, argument: String)
        fun openTransactionDetailsScreen(transaction: HistoryTransaction)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory()
        fun refresh()
        fun fetchNextPage()
        fun onItemClicked(transaction: HistoryTransaction)
        fun closeAccount()
    }
}

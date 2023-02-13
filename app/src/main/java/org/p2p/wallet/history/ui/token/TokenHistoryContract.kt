package org.p2p.wallet.history.ui.token

import androidx.annotation.StringRes
import androidx.lifecycle.DefaultLifecycleObserver
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails

interface TokenHistoryContract {

    interface View : MvpView {
        fun showActionButtons(actionButtons: List<ActionButton>)
        fun showPagingState(newState: PagingState)
        fun showRefreshing(isRefreshing: Boolean)
        fun showError(@StringRes resId: Int, argument: String)
        fun scrollToTop()
        fun showHistory(history: List<HistoryItem>)
        fun showDetailsScreen(transaction: HistoryTransaction)
        fun openSellTransactionDetails(sellTransaction: SellTransactionViewDetails)
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
        fun loadHistory()
        fun onItemClicked(historyItem: HistoryItem)
        fun closeAccount()
        fun loadNextHistoryPage()
        fun retryLoad()
    }
}

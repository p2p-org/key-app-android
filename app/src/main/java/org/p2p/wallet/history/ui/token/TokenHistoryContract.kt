package org.p2p.wallet.history.ui.token

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.moonpay.model.SellTransaction

interface TokenHistoryContract {

    interface View : MvpView {
        fun showActionButtons(actionButtons: List<ActionButton>)
        fun showPagingState(newState: PagingState)
        fun showRefreshing(isRefreshing: Boolean)
        fun showError(@StringRes resId: Int, argument: String)
        fun scrollToTop()
        fun showHistory(
            transactions: List<HistoryTransaction>,
            sellTransactions: List<SellTransaction>
        )
        fun showDetailsScreen(transaction: HistoryTransaction)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory()
        fun onItemClicked(transaction: HistoryTransaction)
        fun closeAccount()
        fun loadNextHistoryPage()
        fun retryLoad()
        fun updateSellTransactions()
    }
}

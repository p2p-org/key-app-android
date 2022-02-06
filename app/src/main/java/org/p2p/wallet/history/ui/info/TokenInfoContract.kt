package org.p2p.wallet.history.ui.info

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.ActionButtonsView
import org.p2p.wallet.history.model.HistoryTransaction

interface TokenInfoContract {

    interface View : MvpView {
        fun showLoading(isLoading: Boolean)
        fun showHistory(transactions: List<HistoryTransaction>)
        fun showActions(items: List<ActionButtonsView.ActionButton>)
        fun showPagingState(newState: PagingState)
        fun showRefreshing(isRefreshing: Boolean)
        fun showError(@StringRes resId: Int, argument: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory()
        fun refresh()
        fun fetchNextPage()
    }
}
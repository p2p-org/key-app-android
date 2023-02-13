package org.p2p.wallet.history.ui.historylist

import androidx.lifecycle.DefaultLifecycleObserver
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.model.HistoryItem

interface HistoryListViewContract {
    interface View : MvpView {
        fun showPagingState(state: PagingState)
        fun showRefreshing(isRefreshing: Boolean)
        fun scrollToTop()
        fun showHistory(history: List<HistoryItem>)
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
        fun loadHistory()
        fun refreshHistory()
        fun loadNextHistoryPage()
    }
}

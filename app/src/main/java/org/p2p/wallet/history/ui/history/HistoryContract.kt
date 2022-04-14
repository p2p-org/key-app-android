package org.p2p.wallet.history.ui.history

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.recycler.PagingState

interface HistoryContract {
    interface View : MvpView {
        fun showPagingState(state: PagingState)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory(isRefresh: Boolean)
    }
}

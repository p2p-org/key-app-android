package org.p2p.wallet.history.ui.history

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.PagingState
import org.p2p.wallet.history.model.HistoryTransaction

interface HistoryContract {

    interface View : MvpView {
        fun showHistory(transactions: List<HistoryTransaction>)
        fun showPagingState(newState: PagingState)
        fun showError(@StringRes resId: Int, argument: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun refreshHistory()
        fun loadHistory(publicKey: String, tokenSymbol: String)
    }
}
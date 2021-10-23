package org.p2p.wallet.history.ui.main

import androidx.annotation.StringRes
import com.github.mikephil.charting.data.Entry
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.PagingState
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.history.model.HistoryTransaction

interface HistoryContract {

    interface View : MvpView {
        fun showHistory(transactions: List<HistoryTransaction>)
        fun showChartData(entries: List<Entry>)
        fun showPagingState(newState: PagingState)
        fun showLoading(isLoading: Boolean)
        fun showSolAddress(sol: Token.Active)
        fun showError(@StringRes resId: Int, argument: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadSolAddress()
        fun loadHistory(publicKey: String, tokenSymbol: String)
        fun loadDailyChartData(tokenSymbol: String, days: Int)
        fun loadHourlyChartData(tokenSymbol: String, hours: Int)
    }
}
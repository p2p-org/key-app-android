package com.p2p.wallet.token.ui

import com.github.mikephil.charting.data.Entry
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.token.model.Transaction

interface TokenDetailsContract {

    interface View : MvpView {
        fun showHistory(transactions: List<Transaction>)
        fun showChartData(entries: List<Entry>)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadHistory(publicKey: String, tokenSymbol: String)
        fun loadDailyChartData(tokenSymbol: String, days: Int)
        fun loadHourlyChartData(tokenSymbol: String, hours: Int)
    }
}
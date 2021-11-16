package org.p2p.wallet.history.ui.info

import androidx.annotation.StringRes
import com.github.mikephil.charting.data.Entry
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface TokenInfoContract {

    interface View : MvpView {
        fun showChartData(entries: List<Entry>)
        fun showError(@StringRes resId: Int, argument: String)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadDailyChartData(tokenSymbol: String, days: Int)
        fun loadHourlyChartData(tokenSymbol: String, hours: Int)
    }
}
package org.p2p.wallet.solend.ui.earn

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.solend.model.SolendDepositToken

interface SolendEarnContract {

    interface View : MvpView {
        fun showRefreshing(isRefreshing: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showDeposits(deposits: List<SolendDepositToken>)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun refresh()
    }
}

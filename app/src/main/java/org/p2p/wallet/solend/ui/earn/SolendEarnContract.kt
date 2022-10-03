package org.p2p.wallet.solend.ui.earn

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.solend.model.SolendDepositToken

interface SolendEarnContract {

    interface View : MvpView {
        fun showLoading(isLoading: Boolean)
        fun showTokens(tokens: List<SolendDepositToken>)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
    }
}

package org.p2p.wallet.solend.ui.deposits

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.solend.ui.deposits.adapter.TokenDepositItemClickListener

interface SolendUserDepositsContract {

    interface View : MvpView {
        fun showLoading(isLoading: Boolean)
        fun showTokens(tokens: List<SolendDepositToken>)
    }

    interface Presenter : MvpPresenter<View>, TokenDepositItemClickListener
}

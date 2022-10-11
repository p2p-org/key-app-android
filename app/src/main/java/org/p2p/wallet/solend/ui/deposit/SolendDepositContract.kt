package org.p2p.wallet.solend.ui.deposit

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.solend.model.SolendDepositToken

interface SolendDepositContract {

    interface View : MvpView {
        fun showTokenToDeposit(depositToken: SolendDepositToken, withChevron: Boolean)
        fun showTokensToDeposit(depositTokens: List<SolendDepositToken>)
    }

    interface Presenter : MvpPresenter<View> {
        fun selectTokenToDeposit(tokenToDeposit: SolendDepositToken)
        fun onTokenDepositClicked()
    }
}

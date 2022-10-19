package org.p2p.wallet.solend.ui.withdraw

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.solend.model.SolendDepositToken

interface SolendWithdrawContract {

    interface View : MvpView {
        fun showTokenToWithdraw(depositToken: SolendDepositToken.Active, withChevron: Boolean)
        fun showTokensToWithdraw(depositTokens: List<SolendDepositToken>)
    }

    interface Presenter : MvpPresenter<View> {
        fun selectTokenToWithdraw(tokenToWithdraw: SolendDepositToken.Active)
        fun onTokenWithdrawClicked()
    }
}

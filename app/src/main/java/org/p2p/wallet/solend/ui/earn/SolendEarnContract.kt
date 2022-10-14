package org.p2p.wallet.solend.ui.earn

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.widget.earnwidget.EarnWidgetState
import org.p2p.wallet.solend.model.SolendDepositToken

interface SolendEarnContract {

    interface View : MvpView {
        fun bindWidgetActionButton(callback: () -> Unit)
        fun showWidgetState(state: EarnWidgetState)
        fun showRefreshing(isRefreshing: Boolean)
        fun showLoading(isLoading: Boolean)
        fun setRatesErrorVisibility(isVisible: Boolean)
        fun showDepositTopUp(deposit: SolendDepositToken)
        fun showDepositToSolend(deposit: SolendDepositToken)
        fun showAvailableDeposits(deposits: List<SolendDepositToken>)
        fun navigateToUserDeposits(deposits: List<SolendDepositToken.Active>)
        fun showSolendOnboarding()
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun refresh()
        fun onDepositTokenClicked(deposit: SolendDepositToken)
    }
}

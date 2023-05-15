package org.p2p.wallet.history.ui.token

import androidx.annotation.StringRes
import androidx.lifecycle.DefaultLifecycleObserver
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton

interface TokenHistoryContract {

    interface View : MvpView {
        fun showActionButtons(actionButtons: List<ActionButton>)
        fun showError(@StringRes resId: Int, argument: String)
        fun showDetailsScreen(transactionId: String)
        fun openSellTransactionDetails(transactionId: String)
        fun openOldReceiveInSolana()
        fun showReceiveNetworkDialog()
        fun openReceiveInSolana()
        fun openReceiveInEthereum()
        fun renderTokenAmounts(token: Token.Active)
        fun loadTokenHistoryList()
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
        fun closeAccount()

        fun onReceiveClicked()

        fun onTransactionClicked(transactionId: String)
        fun onSellTransactionClicked(transactionId: String)
    }
}

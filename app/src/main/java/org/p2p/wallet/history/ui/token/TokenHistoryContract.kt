package org.p2p.wallet.history.ui.token

import androidx.annotation.StringRes
import androidx.lifecycle.DefaultLifecycleObserver
import org.p2p.core.token.Token
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.transaction.model.NewShowProgress

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
        fun renderTokenPnl(pnl: TextViewCellModel?)
        fun hideTokenPnl()
        fun showPnlDetails(pnlPercentage: String)
        fun loadTokenHistoryList()
        fun showProgressDialog(bundleId: String, progressDetails: NewShowProgress)
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
        fun closeAccount()

        fun onReceiveClicked()

        fun onTransactionClicked(transactionId: String)
        fun onSellTransactionClicked(transactionId: String)
        fun onBridgePendingClaimClicked(transactionId: String)
        fun onBridgePendingSendClicked(transactionId: String)
        fun onTokenPnlClicked()
    }
}

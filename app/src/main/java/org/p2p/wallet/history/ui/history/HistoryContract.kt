package org.p2p.wallet.history.ui.history

import androidx.lifecycle.DefaultLifecycleObserver
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.transaction.model.NewShowProgress

interface HistoryContract {
    interface View : MvpView {
        fun openTransactionDetailsScreen(transactionId: String)
        fun showBuyScreen(token: Token)
        fun openSellTransactionDetails(transactionId: String)
        fun showProgressDialog(bundleId: String, progressDetails: NewShowProgress)
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
        fun onBuyClicked()
        fun onTransactionClicked(transactionId: String)
        fun onSellTransactionClicked(transactionId: String)
        fun onClaimPendingClicked(transactionId: String)
        fun onSendPendingClicked(transactionId: String)
    }
}

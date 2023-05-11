package org.p2p.wallet.history.ui.history

import androidx.lifecycle.DefaultLifecycleObserver
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface HistoryContract {
    interface View : MvpView {
        fun openTransactionDetailsScreen(transactionId: String)
        fun showBuyScreen(token: Token)
        fun openSellTransactionDetails(transactionId: String)
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
        fun onBuyClicked()
        fun onTransactionClicked(transactionId: String)
        fun onSellTransactionClicked(transactionId: String)
        fun onClaimPendingClicked(transactionId: String)
        fun onSendPendingClicked(transactionId: String)
    }
}

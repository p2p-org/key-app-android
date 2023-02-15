package org.p2p.wallet.history.ui.history

import androidx.lifecycle.DefaultLifecycleObserver
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails

interface HistoryContract {
    interface View : MvpView {
        fun openTransactionDetailsScreen(transaction: HistoryTransaction)
        fun showBuyScreen(token: Token)

        fun openSellTransactionDetails(sellTransaction: SellTransactionViewDetails)
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
        fun onBuyClicked()
        fun onTransactionClicked(transaction: HistoryTransaction)
        fun onSellTransactionClicked(sellTransactionDetails: SellTransactionViewDetails)
    }
}

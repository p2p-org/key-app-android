package org.p2p.wallet.renbtc.ui.transactions

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import kotlinx.coroutines.launch
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.renbtc.model.RenTransaction

class RenTransactionsPresenter(
    private val renBtcInteractor: RenBtcInteractor,
    private val receiveAnalytics: ReceiveAnalytics
) : BasePresenter<RenTransactionsContract.View>(),
    RenTransactionsContract.Presenter {

    override fun loadTransactions() {
        launch {
            val transactions = renBtcInteractor.getAllTransactions()
            view?.showTransactions(transactions)
        }
    }

    override fun onTransactionClicked(item: RenTransaction) {
        receiveAnalytics.logReceiveShowingStatus()
        view?.showTransaction(item)
    }
}

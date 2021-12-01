package org.p2p.wallet.renbtc.ui.transactions

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import kotlinx.coroutines.launch

class RenTransactionsPresenter(
    private val renBtcInteractor: RenBtcInteractor
) : BasePresenter<RenTransactionsContract.View>(),
    RenTransactionsContract.Presenter {

    override fun loadTransactions() {
        launch {
            val transactions = renBtcInteractor.getAllTransactions()
            view?.showTransactions(transactions)
        }
    }
}
package org.p2p.wallet.renbtc.ui.transactions

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.renbtc.model.RenTransaction

interface RenTransactionsContract {

    interface View : MvpView {
        fun showTransactions(transactions: List<RenTransaction>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadTransactions()
    }
}
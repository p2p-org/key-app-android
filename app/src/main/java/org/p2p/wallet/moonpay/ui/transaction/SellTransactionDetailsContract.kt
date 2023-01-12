package org.p2p.wallet.moonpay.ui.transaction

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SellTransactionDetailsContract {
    interface View : MvpView {
        fun close()
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun onCancelTransactionClicked()
        fun onRemoveFromHistoryClicked()
    }
}

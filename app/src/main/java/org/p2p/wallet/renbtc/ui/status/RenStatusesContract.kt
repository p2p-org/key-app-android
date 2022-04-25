package org.p2p.wallet.renbtc.ui.status

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.renbtc.model.RenTransactionStatus

interface RenStatusesContract {

    interface View : MvpView {
        fun showStatuses(statuses: List<RenTransactionStatus>)
        fun showStatusesNotFound()
    }

    interface Presenter : MvpPresenter<View> {
        fun subscribe(transactionId: String)
    }
}

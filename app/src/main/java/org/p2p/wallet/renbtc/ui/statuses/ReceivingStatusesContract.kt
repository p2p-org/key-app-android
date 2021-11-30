package org.p2p.wallet.renbtc.ui.statuses

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.renbtc.model.RenVMStatus

interface ReceivingStatusesContract {

    interface View : MvpView {
        fun showData(statuses: List<RenVMStatus>)
    }

    interface Presenter : MvpPresenter<View> {
        fun subscribe()
    }
}
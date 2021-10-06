package com.p2p.wallet.renbtc.ui.statuses

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.renbtc.model.RenVMStatus

interface ReceivingStatusesContract {

    interface View : MvpView {
        fun showData(statuses: List<RenVMStatus>)
    }

    interface Presenter : MvpPresenter<View> {
        fun subscribe()
    }
}
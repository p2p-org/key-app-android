package com.p2p.wallet.main.ui.receive.statuses

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.main.model.ReceiveStatus

interface ReceivingStatusesContract {

    interface View : MvpView {
        fun showData(statuses: List<ReceiveStatus>)
    }

    interface Presenter : MvpPresenter<View> {
        fun subscribe()
    }
}
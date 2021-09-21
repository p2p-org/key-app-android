package com.p2p.wallet.renBTC.renbtc

import android.content.Context
import android.graphics.Bitmap
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.renBTC.model.RenVMStatus

interface RenBTCContract {

    interface View : MvpView {
        fun showActiveState(address: String, remaining: String, minTransaction: String?)
        fun updateTimer(remaining: String)

        fun showIdleState()
        fun showLatestStatus(statuses: List<RenVMStatus>)

        fun renderQr(qrBitmap: Bitmap?)
        fun showLoading(isLoading: Boolean)
        fun showQrLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun subscribe()
        fun checkActiveSession(context: Context)
        fun startNewSession(context: Context)
        fun cancelTimer()
    }
}
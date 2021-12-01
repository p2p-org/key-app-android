package org.p2p.wallet.renbtc.ui.main

import android.content.Context
import android.graphics.Bitmap
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface RenBTCContract {

    interface View : MvpView {
        fun showActiveState(address: String, remaining: String, fee: String)
        fun updateTimer(remaining: String)

        fun showIdleState()

        fun renderQr(qrBitmap: Bitmap?)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun subscribe()
        fun checkActiveSession(context: Context)
        fun startNewSession(context: Context)
        fun cancelTimer()
    }
}
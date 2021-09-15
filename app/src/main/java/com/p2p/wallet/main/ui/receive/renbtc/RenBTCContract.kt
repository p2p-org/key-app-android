package com.p2p.wallet.main.ui.receive.renbtc

import android.graphics.Bitmap
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface RenBTCContract {

    interface View : MvpView {
        fun showActiveState(address: String, remaining: String)
        fun updateTimer(remaining: String)

        fun showIdleState()

        fun renderQr(qrBitmap: Bitmap?)
        fun showLoading(isLoading: Boolean)
        fun showQrLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadSessionIfExists()
        fun showAddress()
        fun cancelTimer()
    }
}
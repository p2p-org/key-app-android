package com.p2p.wallet.auth.ui.username

import android.graphics.Bitmap
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface UsernameContract {

    interface View : MvpView {
        fun showName(name: String?)
        fun renderQr(qrBitmap: Bitmap?)
        fun showAddress(address: String?)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun saveQr(name: String)
    }
}
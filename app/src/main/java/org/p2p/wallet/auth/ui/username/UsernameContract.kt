package org.p2p.wallet.auth.ui.username

import android.graphics.Bitmap
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface UsernameContract {

    interface View : MvpView {
        fun showName(name: String?)
        fun renderQr(qrBitmap: Bitmap?)
        fun showAddress(address: String?)
        fun copySuccess()
        fun saveSuccess()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun saveQr(name: String)
    }
}
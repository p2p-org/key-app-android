package org.p2p.wallet.auth.ui.username

import android.graphics.Bitmap
import androidx.annotation.StringRes
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface UsernameContract {

    interface View : MvpView {
        fun showUsername(username: Username)
        fun renderQr(qrBitmap: Bitmap)
        fun showAddress(address: String)
        fun showToastMessage(@StringRes messageRes: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun saveQr(name: String, qrBitmap: Bitmap)
    }
}
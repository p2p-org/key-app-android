package org.p2p.wallet.receive.token

import android.graphics.Bitmap
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.Token
import java.io.File

interface ReceiveTokenContract {

    interface View : MvpView {
        fun renderQr(qrBitmap: Bitmap?)
        fun showReceiveToken(token: Token.Active)
        fun showUserData(userPublicKey: String, directPublicKey: String, username: Username?)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showQrLoading(isLoading: Boolean)
        fun showToastMessage(resId: Int)
        fun showNetwork()
        fun showShareQr(qrImage: File, qrValue: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun saveQr(name: String, bitmap: Bitmap, shareAfter: Boolean = false)
        fun onNetworkClicked()
    }
}

package org.p2p.wallet.receive.solana

import android.graphics.Bitmap
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewReceiveSolanaContract {
    interface View : MvpView {
        fun showQrAndUsername(qrBitmap: Bitmap, username: String?)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadQr(tokenAddress: String)
    }
}

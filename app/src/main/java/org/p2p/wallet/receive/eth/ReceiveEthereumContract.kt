package org.p2p.wallet.receive.eth

import android.graphics.Bitmap
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ReceiveEthereumContract {
    interface View : MvpView {
        fun showQrAndAddress(qrBitmap: Bitmap, addressInHexString: String)

        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
    }
}

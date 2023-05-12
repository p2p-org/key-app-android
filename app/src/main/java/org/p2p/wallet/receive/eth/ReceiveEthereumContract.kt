package org.p2p.wallet.receive.eth

import android.graphics.Bitmap
import java.math.BigDecimal
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ReceiveEthereumContract {
    interface View : MvpView {
        fun showQrAndAddress(qrBitmap: Bitmap, addressInHexString: String)
        fun showLoading(isLoading: Boolean)
        fun setMinAmountForFreeFee(minAmountForFreeFee: BigDecimal)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
    }
}

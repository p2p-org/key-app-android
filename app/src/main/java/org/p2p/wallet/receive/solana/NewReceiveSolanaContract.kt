package org.p2p.wallet.receive.solana

import android.graphics.Bitmap
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewReceiveSolanaContract {
    interface View : MvpView {
        fun initView(qrBitmap: Bitmap, username: String?, tokenAddress: String)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
    }
}

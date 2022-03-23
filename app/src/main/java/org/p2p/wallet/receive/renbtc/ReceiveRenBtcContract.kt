package org.p2p.wallet.receive.renbtc

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.io.File

interface ReceiveRenBtcContract {

    interface View : MvpView {
        fun showActiveState(address: String, remaining: String, fee: String)
        fun updateTimer(remaining: String)
        fun renderQr(qrBitmap: Bitmap?)
        fun showLoading(isLoading: Boolean)
        fun showToastMessage(@StringRes resId: Int)
        fun showTransactionsCount(count: Int)
        fun navigateToSolana()
        fun showNetwork()
        fun showBrowser(url: String)
        fun showStatuses()
        fun showShareQr(qrImage: File, qrValue: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun subscribe()
        fun checkActiveSession(context: Context)
        fun startNewSession(context: Context)
        fun cancelTimer()
        fun saveQr(name: String, bitmap: Bitmap, shareAfter: Boolean = false)
        fun onNetworkClicked()
        fun onBrowserClicked(publicKey: String)
        fun onStatusReceivedClicked()
    }
}

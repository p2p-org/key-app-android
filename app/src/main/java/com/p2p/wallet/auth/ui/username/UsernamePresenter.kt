package com.p2p.wallet.auth.ui.username

import android.graphics.Bitmap
import com.p2p.wallet.auth.interactor.UsernameInteractor
import com.p2p.wallet.common.mvp.BasePresenter

import com.p2p.wallet.qr.interactor.QrCodeInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.CancellationException

class UsernamePresenter(
    private val interactor: UsernameInteractor,
    private val qrCodeInteractor: QrCodeInteractor
) : BasePresenter<UsernameContract.View>(), UsernameContract.Presenter {

    private var qrJob: Job? = null

    private var qrBitmap: Bitmap? = null

    override fun loadData() {
        val address = interactor.getAddress()
        view?.showName(interactor.getName())
        generateQrCode(address)
        view?.showAddress(address)
    }

    private fun generateQrCode(address: String) {
        qrJob?.cancel()
        qrJob = launch {
            try {
                val qr = qrCodeInteractor.generateQrCode(address)
                qrBitmap?.recycle()
                qrBitmap = qr
                view?.renderQr(qr)
            } catch (e: CancellationException) {
                Timber.d("Qr generation was cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Failed to generate qr bitmap")
                view?.showErrorMessage()
            }
        }
    }
}
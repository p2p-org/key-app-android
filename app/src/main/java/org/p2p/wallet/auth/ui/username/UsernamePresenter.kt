package org.p2p.wallet.auth.ui.username

import android.graphics.Bitmap
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter

import org.p2p.wallet.qr.interactor.QrCodeInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import timber.log.Timber
import java.util.concurrent.CancellationException

class UsernamePresenter(
    private val usernameInteractor: UsernameInteractor,
    private val qrCodeInteractor: QrCodeInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) : BasePresenter<UsernameContract.View>(), UsernameContract.Presenter {

    private var qrJob: Job? = null

    private var qrBitmap: Bitmap? = null

    override fun loadData() {
        val publicKey = tokenKeyProvider.publicKey
        val username = usernameInteractor.getUsername()
        view?.showUsername(username)
        generateQrCode(publicKey)
        view?.showAddress(publicKey)
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

    override fun saveQr(name: String) {
        launch {
            qrBitmap?.let {
                usernameInteractor.saveQr(name, it)
                view?.saveSuccess()
            }
        }
    }
}
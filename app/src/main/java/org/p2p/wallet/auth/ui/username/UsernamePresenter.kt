package org.p2p.wallet.auth.ui.username

import android.graphics.Bitmap
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter

import org.p2p.wallet.qr.interactor.QrCodeInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import timber.log.Timber
import java.util.concurrent.CancellationException

class UsernamePresenter(
    private val usernameInteractor: UsernameInteractor,
    private val qrCodeInteractor: QrCodeInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) : BasePresenter<UsernameContract.View>(), UsernameContract.Presenter {

    private var qrJob: Job? = null

    override fun loadData() {
        val publicKey = tokenKeyProvider.publicKey
        val username = usernameInteractor.getUsername() ?: return
        view?.showUsername(username)
        view?.showAddress(publicKey)

        generateQrCode(publicKey)
    }

    private fun generateQrCode(address: String) {
        qrJob?.cancel()
        qrJob = launch {
            try {
                val qr = qrCodeInteractor.generateQrCode(address)
                view?.renderQr(qr)
            } catch (e: CancellationException) {
                Timber.d("Qr generation was cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Failed to generate qr bitmap")
                view?.showErrorMessage()
            }
        }
    }

    override fun saveQr(name: String, bitmap: Bitmap, shareAfter: Boolean) {
        launch {
            try {
                val savedFile = usernameInteractor.saveQr(name, bitmap)
                if (shareAfter) {
                    savedFile?.let {
                        view?.showShareQr(it, name)
                    } ?: Timber.e("Error on saving QR file == null")
                } else {
                    view?.showToastMessage(R.string.auth_saved)
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error on saving QR")
                view?.showErrorMessage(e)
            }
        }
    }
}

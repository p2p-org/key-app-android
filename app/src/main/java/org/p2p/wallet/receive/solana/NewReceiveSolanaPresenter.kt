package org.p2p.wallet.receive.solana

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.qr.interactor.QrCodeInteractor
import timber.log.Timber
import java.util.concurrent.CancellationException

class NewReceiveSolanaPresenter(
    private val qrCodeInteractor: QrCodeInteractor,
    private val usernameInteractor: UsernameInteractor
) : BasePresenter<NewReceiveSolanaContract.View>(),
    NewReceiveSolanaContract.Presenter {

    override fun loadQr(tokenAddress: String) {
        launch {
            try {
                view?.showLoading(isLoading = true)
                val qr = qrCodeInteractor.generateQrCode(tokenAddress)
                val username = usernameInteractor.getUsername()?.fullUsername.orEmpty()
                view?.showQrAndUsername(qr, username)
            } catch (e: CancellationException) {
                Timber.d("Qr generation was cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Failed to generate qr bitmap")
                view?.showErrorMessage()
            } finally {
                view?.showLoading(isLoading = false)
            }
        }
    }
}

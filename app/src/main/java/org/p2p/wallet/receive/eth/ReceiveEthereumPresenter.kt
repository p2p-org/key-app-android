package org.p2p.wallet.receive.eth

import timber.log.Timber
import java.util.concurrent.CancellationException
import kotlinx.coroutines.launch
import org.p2p.ethereumkit.external.repository.EthereumRepository
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.qr.interactor.QrCodeInteractor

class ReceiveEthereumPresenter(
    private val ethereumRepository: EthereumRepository,
    private val qrCodeInteractor: QrCodeInteractor
) : BasePresenter<ReceiveEthereumContract.View>(),
    ReceiveEthereumContract.Presenter {

    override fun load() {
        launch {
            try {
                view?.showLoading(isLoading = true)
                val tokenAddressInHexString = ethereumRepository.getAddress().hex
                val qr = qrCodeInteractor.generateQrCode(tokenAddressInHexString)
                view?.showQrAndAddress(qr, tokenAddressInHexString)
            } catch (e: CancellationException) {
                Timber.d("Qr generation was cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Failed to generate qr bitmap")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(isLoading = false)
            }
        }
    }
}

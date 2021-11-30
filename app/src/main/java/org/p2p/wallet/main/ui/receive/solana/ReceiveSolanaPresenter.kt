package org.p2p.wallet.main.ui.receive.solana

import android.graphics.Bitmap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.qr.interactor.QrCodeInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber
import java.util.concurrent.CancellationException
import kotlin.properties.Delegates

private const val DELAY_IN_MILLIS = 200L

class ReceiveSolanaPresenter(
    private val defaultToken: Token.Active?,
    private val userInteractor: UserInteractor,
    private val qrCodeInteractor: QrCodeInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) : BasePresenter<ReceiveSolanaContract.View>(), ReceiveSolanaContract.Presenter {

    private var qrJob: Job? = null

    private var qrBitmap: Bitmap? = null

    private var token: Token.Active? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showReceiveToken(newValue)
    }

    override fun attach(view: ReceiveSolanaContract.View) {
        super.attach(view)
        qrBitmap?.let { view.renderQr(it) }
    }

    override fun loadData() {
        launch {
            view?.showFullScreenLoading(true)
            val tokens = userInteractor.getUserTokens()
            val receive = defaultToken ?: tokens.firstOrNull() ?: return@launch
            token = receive

            val publicKey = tokenKeyProvider.publicKey
            val username = usernameInteractor.getUsername()
            view?.showUserData(publicKey, username)

            generateQrCode(publicKey)

            view?.showFullScreenLoading(false)
        }
    }

    private fun generateQrCode(address: String) {
        qrJob?.cancel()
        qrJob = launch {
            try {
                view?.showQrLoading(true)
                delay(DELAY_IN_MILLIS)
                val qr = qrCodeInteractor.generateQrCode(address)
                qrBitmap?.recycle()
                qrBitmap = qr
                view?.renderQr(qr)
            } catch (e: CancellationException) {
                Timber.d("Qr generation was cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Failed to generate qr bitmap")
                view?.showErrorMessage()
            } finally {
                view?.showQrLoading(false)
            }
        }
    }
}
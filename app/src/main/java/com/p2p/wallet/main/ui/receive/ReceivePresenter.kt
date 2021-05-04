package com.p2p.wallet.main.ui.receive

import android.graphics.Bitmap
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.qr.interactor.QrCodeInteractor
import com.p2p.wallet.qr.model.QrColors
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.CancellationException
import kotlin.properties.Delegates

private const val DELAY_IN_MILLIS = 200L

class ReceivePresenter(
    private val userInteractor: UserInteractor,
    private val qrCodeInteractor: QrCodeInteractor,
    private val qrColors: QrColors
) : BasePresenter<ReceiveContract.View>(), ReceiveContract.Presenter {

    private var qrJob: Job? = null

    private var qrBitmap: Bitmap? = null

    private var token: Token? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) view?.showReceiveToken(newValue)
    }

    override fun attach(view: ReceiveContract.View) {
        super.attach(view)
        qrBitmap?.let { view.renderQr(it) }
    }

    override fun setReceiveToken(newToken: Token) {
        token = newToken

        generateQrCode(newToken.depositAddress)
    }

    override fun loadData() {
        launch {
            view?.showFullScreenLoading(true)
            val tokens = userInteractor.getTokens()
            val receive = tokens.firstOrNull() ?: return@launch
            token = receive

            generateQrCode(receive.depositAddress)

            view?.showFullScreenLoading(false)
        }
    }

    override fun loadTokensForSelection() {
        launch {
            val tokens = userInteractor.getTokens()
            view?.navigateToTokenSelection(tokens)
        }
    }

    private fun generateQrCode(address: String) {
        qrJob?.cancel()
        qrJob = launch {
            try {
                view?.showQrLoading(true)

                delay(DELAY_IN_MILLIS)
                val qr = qrCodeInteractor.generateQrCode(address, qrColors)
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
package com.p2p.wallet.main.ui.receive

import android.graphics.Bitmap
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.qr.interactor.QrCodeInteractor
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.CancellationException
import kotlin.properties.Delegates

private const val DELAY_IN_MILLIS = 200L

class ReceivePresenter(
    private val defaultToken: Token?,
    private val userInteractor: UserInteractor,
    private val qrCodeInteractor: QrCodeInteractor
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

    override fun loadData() {
        launch {
            view?.showFullScreenLoading(true)
            val tokens = userInteractor.getUserTokens()
            val receive = defaultToken ?: tokens.firstOrNull() ?: return@launch
            token = receive

            val sol = tokens.first { it.isSOL }
            view?.showSolAddress(sol)
            generateQrCode(sol.publicKey)

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
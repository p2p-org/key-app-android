package org.p2p.wallet.receive.solana

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.core.token.Token
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.qr.interactor.QrCodeInteractor
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
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
    private val tokenKeyProvider: TokenKeyProvider,
    private val receiveAnalytics: ReceiveAnalytics,
    private val context: Context
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
            receiveAnalytics.logReceiveViewed(isUsernameClaimed = !username?.value.isNullOrEmpty())
            view?.showFullScreenLoading(false)
        }
    }

    override fun saveQr(name: String, bitmap: Bitmap, shareText: String?) {
        launch {
            try {
                val savedFile = usernameInteractor.saveQr(name, bitmap, forSharing = shareText != null)
                shareText?.let { textToShare ->
                    savedFile?.let { file ->
                        view?.showShareQr(file, textToShare)
                    } ?: Timber.e("Error on saving QR file == null")
                } ?: view?.showToastMessage(R.string.auth_saved)
            } catch (e: Throwable) {
                Timber.e(e, "Error on saving QR")
                view?.showErrorMessage(e)
            }
        }
    }

    override fun onNetworkClicked() {
        receiveAnalytics.logReceiveChangingNetwork(ReceiveAnalytics.AnalyticsReceiveNetwork.SOLANA)
        view?.showNetwork()
    }

    override fun onBrowserClicked(publicKey: String) {
        receiveAnalytics.logReceiveViewingExplorer(ReceiveAnalytics.AnalyticsReceiveNetwork.SOLANA)
        val url = context.getString(R.string.solanaWalletExplorer, publicKey)
        view?.showBrowser(url)
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

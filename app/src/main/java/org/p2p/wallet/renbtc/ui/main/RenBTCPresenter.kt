package org.p2p.wallet.renbtc.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.os.CountDownTimer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.kits.renBridge.LockAndMint
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.qr.interactor.QrCodeInteractor
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.renbtc.model.RenBtcSession
import org.p2p.wallet.renbtc.service.RenVMService
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.scaleMedium
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

private const val DELAY_IN_MILLIS = 200L
private const val ONE_SECOND_IN_MILLIS = 1000L

class RenBTCPresenter(
    private val interactor: RenBtcInteractor,
    private val qrCodeInteractor: QrCodeInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val receiveAnalytics: ReceiveAnalytics,
    private val context: Context
) : BasePresenter<RenBTCContract.View>(), RenBTCContract.Presenter {

    private var sessionTimer: CountDownTimer? = null

    private var qrJob: Job? = null

    private var qrBitmap: Bitmap? = null

    override fun subscribe() {
        launch {
            interactor.getSessionFlow().collect { session ->
                when (session) {
                    is RenBtcSession.Error -> {
                        view?.showErrorMessage(session.throwable)
                        view?.showLoading(false)
                    }
                    is RenBtcSession.Active -> {
                        handleSession(session.session)
                        view?.showLoading(false)
                    }
                    is RenBtcSession.Loading -> {
                        view?.showLoading(true)
                    }
                }
            }
        }
    }

    override fun startNewSession(context: Context) {
        RenVMService.startWithNewSession(context)
    }

    override fun checkActiveSession(context: Context) {
        RenVMService.startWithCheck(context)
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
        receiveAnalytics.logReceiveChangingNetwork(ReceiveAnalytics.AnalyticsReceiveNetwork.BITCOIN)
        view?.showNetwork()
    }

    override fun onBrowserClicked(publicKey: String) {
        receiveAnalytics.logReceiveViewingExplorer(ReceiveAnalytics.AnalyticsReceiveNetwork.BITCOIN)
        val url = context.getString(R.string.bitcoinExplorer, publicKey)
        view?.showBrowser(url)
    }

    override fun onStatusReceivedClicked() {
        receiveAnalytics.logReceiveShowingStatuses()
        view?.showStatuses()
    }

    override fun cancelTimer() {
        sessionTimer?.cancel()
        sessionTimer = null
    }

    private fun handleSession(session: LockAndMint.Session?) {
        if (session != null && session.isValid) {
            val remaining = session.expiryTime - System.currentTimeMillis()
            val fee = calculateFee(session)
            view?.showActiveState(session.gatewayAddress, remaining.toDateString(), fee.toString())

            startTimer(remaining)
            generateQrCode(session.gatewayAddress)
            loadTransactionCount()
            view?.showLoading(false)
        } else {
            // TODO navigate so solana broke logic
        }
    }

    private fun calculateFee(session: LockAndMint.Session) =
        session.fee.fromLamports(org.p2p.wallet.receive.renbtc.BTC_DECIMALS).multiply(BigDecimal(2)).scaleMedium()

    private fun generateQrCode(address: String) {
        if (qrJob?.isActive == true) return

        qrJob = launch {
            try {
                delay(DELAY_IN_MILLIS)
                val qr = qrCodeInteractor.generateQrCode(address)
                qrBitmap?.recycle()
                qrBitmap = qr
                view?.renderQr(qr)
            } catch (e: CancellationException) {
                Timber.d("RenBTC qr generation was cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Failed to generate qr bitmap")
                view?.showErrorMessage()
            }
        }
    }

    private fun loadTransactionCount() {
        launch {
            val transactions = interactor.getAllTransactions()
            view?.showTransactionsCount(transactions.size)
        }
    }

    private fun startTimer(time: Long) {
        if (sessionTimer != null) return

        sessionTimer = object : CountDownTimer(time, ONE_SECOND_IN_MILLIS) {
            override fun onTick(millisUntilFinished: Long) {
                val timeRemaining = millisUntilFinished.toDateString()
                view?.updateTimer(timeRemaining)
            }

            override fun onFinish() {
                view?.navigateToSolana()
            }
        }

        sessionTimer!!.start()
    }

    private fun Long.toDateString(): String {
        val oneDay = TimeUnit.DAYS.toMillis(1)
        var millis = this - oneDay
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        return "${hours.toTimeFormat()}:${minutes.toTimeFormat()}:${seconds.toTimeFormat()}"
    }

    @Suppress("MagicNumber")
    private fun Long.toTimeFormat(): String =
        if (this > 10L) this.toString() else "0$this"
}

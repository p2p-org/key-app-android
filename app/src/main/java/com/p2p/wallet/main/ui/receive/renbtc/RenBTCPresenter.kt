package com.p2p.wallet.main.ui.receive.renbtc

import android.graphics.Bitmap
import android.os.CountDownTimer
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.interactor.RenBTCInteractor
import com.p2p.wallet.qr.interactor.QrCodeInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.p2p.solanaj.kits.renBridge.LockAndMint
import timber.log.Timber
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

private const val DELAY_IN_MILLIS = 200L
private const val MILLIS_IN_SECOND = 1000L

class RenBTCPresenter(
    private val interactor: RenBTCInteractor,
    private val qrCodeInteractor: QrCodeInteractor
) : BasePresenter<RenBTCContract.View>(), RenBTCContract.Presenter {

    private var sessionTimer: CountDownTimer? = null

    private var qrJob: Job? = null
    private var pollingJob: Job? = null

    private var qrBitmap: Bitmap? = null

    override fun loadSessionIfExists() {
        view?.showLoading(true)
        launch {
            try {
                val session = interactor.getSession()
                if (session != null) {
                    val remaining = session.expiryTime - System.currentTimeMillis()
                    view?.showActiveState(session.gatewayAddress, remaining.toDateString())

                    startTimer(remaining)
                    startPolling(session)

                    generateQrCode(session.gatewayAddress)
                } else {
                    view?.showIdleState()
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error occurred when searching existing session")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun showAddress() {
        launch {
            try {
                view?.showLoading(true)
                val session = interactor.generateSession()
                val remaining = session.expiryTime - System.currentTimeMillis()

                startTimer(remaining)
                handlePaymentData()
                startPolling(session)

                generateQrCode(session.gatewayAddress)
                view?.showActiveState(session.gatewayAddress, remaining.toDateString())
            } catch (e: Throwable) {
                view?.showErrorMessage(e)
                Timber.e(e, "Failed to generate address")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private suspend fun handlePaymentData() {
        interactor.getPaymentDataFlow().collect { data ->
            // todo: do smth
        }
    }

    private fun startPolling(session: LockAndMint.Session) {
        pollingJob?.cancel()
        pollingJob = launch {
            try {
                interactor.startPolling(session)
            } catch (e: Throwable) {
                Timber.e(e, "Failed to get data from blockstream")
            }
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
                Timber.d("RenBTC qr generation was cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Failed to generate qr bitmap")
                view?.showErrorMessage()
            } finally {
                view?.showQrLoading(false)
            }
        }
    }

    private fun startTimer(time: Long) {
        if (sessionTimer != null) return

        sessionTimer = object : CountDownTimer(time, MILLIS_IN_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                val timeRemaining = millisUntilFinished.toDateString()
                view?.updateTimer(timeRemaining)
            }

            override fun onFinish() {
                view?.showIdleState()
            }
        }

        sessionTimer!!.start()
    }

    override fun cancelTimer() {
        sessionTimer?.cancel()
        sessionTimer = null
    }

    private fun Long.toDateString(): String {
        var millis = this
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        return "$hours:$minutes:$seconds"
    }
}
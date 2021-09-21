package com.p2p.wallet.renBTC.renbtc

import android.content.Context
import android.graphics.Bitmap
import android.os.CountDownTimer
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.qr.interactor.QrCodeInteractor
import com.p2p.wallet.renBTC.interactor.RenBTCInteractor
import com.p2p.wallet.renBTC.service.RenVMService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.p2p.solanaj.kits.renBridge.LockAndMint
import timber.log.Timber
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

private const val DELAY_IN_MILLIS = 200L
private const val ONE_SECOND_IN_MILLIS = 1000L

class RenBTCPresenter(
    private val interactor: RenBTCInteractor,
    private val qrCodeInteractor: QrCodeInteractor
) : BasePresenter<RenBTCContract.View>(), RenBTCContract.Presenter {

    private var sessionTimer: CountDownTimer? = null

    private var qrJob: Job? = null

    private var qrBitmap: Bitmap? = null

    override fun subscribe() {
        launch {
            interactor.getSessionFlow().collect { session ->
                handleSession(session)
            }
        }

        launch {
            interactor.getRenVMStatusFlow().collect { statuses ->
                view?.showLatestStatus(statuses)
            }
        }
    }

    private fun handleSession(session: LockAndMint.Session?) {
        if (session != null && session.isValid) {
            val remaining = session.expiryTime - System.currentTimeMillis()
            view?.showActiveState(session.gatewayAddress, remaining.toDateString(), null)

            startTimer(remaining)
            generateQrCode(session.gatewayAddress)
        } else {
            view?.showIdleState()
        }
    }

    override fun startNewSession(context: Context) {
        launch {
            view?.showLoading(true)
            RenVMService.startWithNewSession(context)
            delay(ONE_SECOND_IN_MILLIS + ONE_SECOND_IN_MILLIS)
            view?.showLoading(false)
        }
    }

    override fun checkActiveSession(context: Context) {
        launch {
            view?.showLoading(true)
            RenVMService.startWithCheck(context)
            delay(ONE_SECOND_IN_MILLIS)
            view?.showLoading(false)
        }
    }

    private fun generateQrCode(address: String) {
        if (qrJob?.isActive == true) return

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

        sessionTimer = object : CountDownTimer(time, ONE_SECOND_IN_MILLIS) {
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
        val oneDay = TimeUnit.DAYS.toMillis(1)
        var millis = this - oneDay
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        return "$hours:$minutes:$seconds"
    }
}
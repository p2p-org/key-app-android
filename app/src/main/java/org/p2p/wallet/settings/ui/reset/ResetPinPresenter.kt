package org.p2p.wallet.settings.ui.reset

import android.os.CountDownTimer
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.auth.model.SignInResult
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.crypto.Cipher

private const val VIBRATE_DURATION = 500L
private const val PIN_CODE_ATTEMPT_COUNT = 3

private const val TIMER_MILLIS = 10000L
private const val TIMER_INTERVAL = 1000L

class ResetPinPresenter(
    private val authInteractor: AuthInteractor
) : BasePresenter<ResetPinContract.View>(), ResetPinContract.Presenter {

    private var isCurrentPinConfirmed = false
    private var createdPin = ""

    private var wrongPinCounter = 0

    private var timer: CountDownTimer? = null

    override fun setPinCode(pinCode: String) {
        if (!isCurrentPinConfirmed) {
            verifyPin(pinCode)
        } else {
            resetPin(pinCode)
        }
    }

    override fun resetPinWithoutBiometrics() {
        resetPinActually()
    }

    override fun resetPinWithBiometrics(cipher: Cipher) {
        resetPinActually(cipher)
    }

    private fun verifyPin(pinCode: String) {
        view?.showLoading(true)
        launch {
            try {
                onSignInResult(authInteractor.signInByPinCode(pinCode))
            } catch (e: Exception) {
                Timber.e(e, "error checking current pin")
                view?.showCurrentPinIncorrectError(PIN_CODE_ATTEMPT_COUNT - wrongPinCounter)
                view?.vibrate(VIBRATE_DURATION)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun onSignInResult(result: SignInResult) {
        when (result) {
            is SignInResult.Success -> {
                isCurrentPinConfirmed = true
                view?.showEnterNewPin()
            }
            SignInResult.WrongPin -> {
                wrongPinCounter++

                if (wrongPinCounter >= PIN_CODE_ATTEMPT_COUNT) {
                    startTimer()
                    view?.vibrate(VIBRATE_DURATION)
                    view?.clearPin()
                    return
                }

                view?.showCurrentPinIncorrectError(PIN_CODE_ATTEMPT_COUNT - wrongPinCounter)
                view?.vibrate(VIBRATE_DURATION)
            }
        } ?: Unit
    }

    private fun resetPin(pinCode: String) {
        if (createdPin.isEmpty()) {
            createdPin = pinCode
            view?.showConfirmNewPin()
            return
        }

        if (createdPin != pinCode) {
            view?.showConfirmationError()
            view?.vibrate(VIBRATE_DURATION)
            return
        }

        if (authInteractor.getBiometricStatus() == BiometricStatus.ENABLED) {
            val cipher = authInteractor.getPinEncodeCipher()
            view?.showBiometricDialog(cipher.value)
        } else {
            resetPinWithoutBiometrics()
        }
    }

    private fun resetPinActually(cipher: Cipher? = null) {
        view?.showLoading(true)
        launch {
            try {
                authInteractor.resetPin(createdPin, cipher?.let { EncodeCipher(it) })
                view?.showResetSuccess()
            } catch (e: Exception) {
                Timber.e(e, "error setting new pin")
                view?.showErrorMessage(e)
                view?.showEnterNewPin()
                view?.vibrate(VIBRATE_DURATION)
            } finally {
                createdPin = ""
                view?.showLoading(false)
            }
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(TIMER_MILLIS, TIMER_INTERVAL) {

            @SuppressWarnings("MagicNumber")
            override fun onTick(millisUntilFinished: Long) {
                val remainingInSeconds = millisUntilFinished / 1000L
                val seconds = remainingInSeconds % 60
                view?.showWalletLocked(seconds)
            }

            override fun onFinish() {
                wrongPinCounter = 0
                view?.showWalletUnlocked()
                view?.showLoading(false)
            }
        }.start()
    }

    override fun detach() {
        timer?.cancel()
        timer = null
        super.detach()
    }
}
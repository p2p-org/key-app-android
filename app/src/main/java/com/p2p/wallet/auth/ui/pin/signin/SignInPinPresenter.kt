package com.p2p.wallet.auth.ui.pin.signin

import android.os.CountDownTimer
import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.auth.model.BiometricStatus
import com.p2p.wallet.auth.model.SignInResult
import com.p2p.wallet.common.crypto.keystore.DecodeCipher
import com.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.crypto.Cipher

private const val VIBRATE_DURATION = 500L
private const val PIN_CODE_ATTEMPT_COUNT = 3

private const val TIMER_MILLIS = 10000L
private const val TIMER_INTERVAL = 1000L

class SignInPinPresenter(
    private val authInteractor: AuthInteractor,
) : BasePresenter<SignInPinContract.View>(), SignInPinContract.Presenter {

    private var wrongPinCounter = 0

    private var timer: CountDownTimer? = null

    override fun signIn(pinCode: String) {
        signInActual {
            authInteractor.signInByPinCode(pinCode)
        }
    }

    override fun signInByBiometric(cipher: Cipher) {
        val decodeCipher = DecodeCipher(cipher)
        signInActual {
            authInteractor.signInByBiometric(decodeCipher)
        }
    }

    override fun checkIfBiometricAvailable() {
        launch {
            try {
                if (authInteractor.getBiometricStatus() == BiometricStatus.ENABLED) {
                    val cipher = authInteractor.getPinDecodeCipher()
                    view?.showBiometricDialog(cipher.value)
                } else {
                    authInteractor.disableBiometricSignIn()
                }
            } catch (e: Throwable) {
                Timber.e(e, "Failed to initialize biometric sign in")
                authInteractor.disableBiometricSignIn()
            }
        }
    }

    override fun onBiometricSignInRequested() {
        if (authInteractor.getBiometricStatus() == BiometricStatus.ENABLED) {
            val cipher = authInteractor.getPinDecodeCipher()
            view?.showBiometricDialog(cipher.value)
        }
    }

    override fun stopTimer() {
        timer?.cancel()
    }

    override fun logout() {
        timer?.cancel()
        authInteractor.logout()
        view?.onLogout()
    }

    private inline fun signInActual(
        crossinline performSignIn: suspend () -> SignInResult
    ) {
        launch {
            try {
                view?.showLoading(true)
                val result = performSignIn()
                handleResult(result)
            } catch (e: Throwable) {
                Timber.e(e, "Error while signing")
                view?.clearPin()
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun handleResult(result: SignInResult) {
        when (result) {
            SignInResult.WrongPin -> {
                wrongPinCounter++

                if (wrongPinCounter >= PIN_CODE_ATTEMPT_COUNT) {
                    startTimer()
                    view?.vibrate(VIBRATE_DURATION)
                    view?.clearPin()
                    return
                }

                view?.showWrongPinError(PIN_CODE_ATTEMPT_COUNT - wrongPinCounter)
                view?.vibrate(VIBRATE_DURATION)
            }
            is SignInResult.Success -> {
                timer?.cancel()
                view?.onSignInSuccess()
            }
        } ?: Unit
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
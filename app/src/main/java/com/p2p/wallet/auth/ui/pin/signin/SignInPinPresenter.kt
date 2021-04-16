package com.p2p.wallet.auth.ui.pin.signin

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

class SignInPinPresenter(
    private val authInteractor: AuthInteractor,
) : BasePresenter<SignInPinContract.View>(), SignInPinContract.Presenter {

    private var wrongPinCounter = 0

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

    override fun logout() {
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
                    view?.showWalletLocked()
                    view?.vibrate(VIBRATE_DURATION)
                    return
                }

                view?.showWrongPinError(PIN_CODE_ATTEMPT_COUNT - wrongPinCounter)
                view?.vibrate(VIBRATE_DURATION)
            }
            is SignInResult.Success ->
                view?.onSignInSuccess()
        } ?: Unit
    }
}
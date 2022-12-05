package org.p2p.wallet.auth.ui.pin.signin

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.auth.model.SignInResult
import org.p2p.wallet.auth.ui.pin.validate.ValidatePinContract
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import javax.crypto.Cipher

private const val VIBRATE_DURATION = 500L

private const val PIN_CODE_WARN_ATTEMPT_COUNT = 3
private const val PIN_CODE_ATTEMPT_COUNT = 5

class ValidatePinPresenter(
    private val authInteractor: AuthInteractor,
    private val authLogoutInteractor: AuthLogoutInteractor,
    private val adminAnalytics: AdminAnalytics,
    private val authAnalytics: AuthAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor
) : BasePresenter<ValidatePinContract.View>(), ValidatePinContract.Presenter {

    private var wrongPinCounter = 0
    private var authType = AuthAnalytics.AuthType.PIN

    override fun signIn(pinCode: String) {
        signInActual {
            authType = AuthAnalytics.AuthType.PIN
            authInteractor.signInByPinCode(pinCode)
        }
    }

    override fun signInByBiometric(cipher: Cipher) {
        val decodeCipher = DecodeCipher(cipher)
        signInActual {
            authType = AuthAnalytics.AuthType.BIOMETRIC
            authInteractor.signInByBiometric(decodeCipher)
        }
    }

    override fun checkIfBiometricAvailable() {
        launch {
            try {
                if (authInteractor.getBiometricStatus() == BiometricStatus.ENABLED) {
                    val cipher = authInteractor.getPinDecodeCipher()
                    view?.apply {
                        showBiometricDialog(cipher.value)
                        setBiometricVisibility(isVisible = true)
                    }
                } else {
                    authInteractor.disableBiometricSignIn()
                    view?.setBiometricVisibility(isVisible = false)
                }
            } catch (e: Throwable) {
                Timber.e(e, "Failed to initialize biometric sign in")
                authInteractor.disableBiometricSignIn()
                view?.setBiometricVisibility(isVisible = false)
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
        launch {
            authLogoutInteractor.onUserLogout()
            view?.onLogout()
        }
    }

    private inline fun signInActual(
        crossinline performSignIn: suspend () -> SignInResult
    ) {
        launch {
            try {
                val result = performSignIn()
                handleResult(result)
            } catch (e: Throwable) {
                Timber.e(e, "Error while signing")
                view?.clearPin()
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private fun handleResult(result: SignInResult) {
        val authResult: AuthAnalytics.AuthResult
        when (result) {
            SignInResult.WrongPin -> {
                wrongPinCounter++
                authResult = AuthAnalytics.AuthResult.ERROR
                if (wrongPinCounter >= PIN_CODE_ATTEMPT_COUNT) {
                    view?.vibrate(VIBRATE_DURATION)
                    view?.clearPin()
                    logout()
                    return
                } else if (wrongPinCounter >= PIN_CODE_WARN_ATTEMPT_COUNT) {
                    view?.vibrate(VIBRATE_DURATION)
                    view?.clearPin()
                    view?.showWarnPinError(PIN_CODE_ATTEMPT_COUNT - wrongPinCounter)
                    return
                } else {
                    view?.showWrongPinError(PIN_CODE_ATTEMPT_COUNT - wrongPinCounter)
                }

                adminAnalytics.logPinRejected(analyticsInteractor.getCurrentScreenName())
                view?.vibrate(VIBRATE_DURATION)
            }
            is SignInResult.Success -> {
                view?.onSignInSuccess()
                authResult = AuthAnalytics.AuthResult.SUCCESS
            }
        }
        authAnalytics.logAuthValidated(result = authResult, authType = authType)
    }
}

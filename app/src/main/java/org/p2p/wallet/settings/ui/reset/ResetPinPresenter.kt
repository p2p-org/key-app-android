package org.p2p.wallet.settings.ui.reset

import android.os.CountDownTimer
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.auth.model.SignInResult
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.utils.emptyString
import timber.log.Timber
import javax.crypto.Cipher

private const val VIBRATE_DURATION = 500L

class ResetPinPresenter(
    private val authInteractor: AuthInteractor,
    private val adminAnalytics: AdminAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor
) : BasePresenter<ResetPinContract.View>(), ResetPinContract.Presenter {

    private var isCurrentPinConfirmed = false
    private var createdPin = emptyString()

    private var timer: CountDownTimer? = null

    init {
        adminAnalytics.logPinResetInvoked()
    }

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

    override fun onSeedPhraseValidated(keys: List<String>) {
        onSignInResult(SignInResult.Success)
    }

    private fun verifyPin(pinCode: String) {
        view?.showLoading(true)
        launch {
            try {
                onSignInResult(authInteractor.signInByPinCode(pinCode))
            } catch (e: Exception) {
                Timber.e(e, "error checking current pin")
                view?.showCurrentPinIncorrectError()
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
                view?.showCurrentPinIncorrectError()
                view?.vibrate(VIBRATE_DURATION)
            }
        }
    }

    private fun resetPin(pinCode: String) {
        val resetResult: AuthAnalytics.AuthResult
        if (createdPin.isEmpty()) {
            createdPin = pinCode
            view?.showConfirmNewPin()
            return
        }

        if (createdPin != pinCode) {
            view?.showConfirmationError()
            view?.vibrate(VIBRATE_DURATION)
            resetResult = AuthAnalytics.AuthResult.ERROR
            return
        }

        if (authInteractor.getBiometricStatus() == BiometricStatus.ENABLED) {
            val cipher = authInteractor.getPinEncodeCipher()
            view?.showBiometricDialog(cipher.value)
        } else {
            resetPinWithoutBiometrics()
        }
        resetResult = AuthAnalytics.AuthResult.SUCCESS
        adminAnalytics.logPinResetValidated(resetResult)
    }

    private fun resetPinActually(cipher: Cipher? = null) {
        view?.showLoading(true)
        launch {
            try {
                authInteractor.resetPin(createdPin, cipher?.let { EncodeCipher(it) })
                adminAnalytics.logPinCreated(currentScreenName = analyticsInteractor.getCurrentScreenName())
                view?.showResetSuccess()
            } catch (e: Exception) {
                Timber.e(e, "error setting new pin")
                view?.showErrorMessage(e)
                view?.showEnterNewPin()
                view?.vibrate(VIBRATE_DURATION)
            } finally {
                createdPin = emptyString()
                view?.showLoading(false)
            }
        }
    }

    override fun detach() {
        timer?.cancel()
        timer = null
        super.detach()
    }
}

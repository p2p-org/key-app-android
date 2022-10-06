package org.p2p.wallet.auth.ui.pin.newcreate

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString
import timber.log.Timber
import javax.crypto.Cipher

private const val VIBRATE_DURATION = 500L

class NewCreatePinPresenter(
    private val analytics: OnboardingAnalytics,
    private val adminAnalytics: AdminAnalytics,
    private val authInteractor: AuthInteractor,
    private val analyticsInteractor: ScreensAnalyticsInteractor
) : BasePresenter<NewCreatePinContract.View>(),
    NewCreatePinContract.Presenter {

    private var createdPin = emptyString()
    private var pinMode = PinMode.CREATE
    private var navigateBackOnBackPressed = false

    override fun setPinMode(pinMode: PinMode) {
        this.pinMode = pinMode
    }

    override fun setPinCode(pinCode: String) {
        if (createdPin.isEmpty()) {
            createdPin = pinCode
            view?.showConfirmation()
            return
        }

        if (pinCode != createdPin) {
            view?.showConfirmationError()
            view?.vibrate(VIBRATE_DURATION)
            adminAnalytics.logPinRejected(ScreenNames.OnBoarding.PIN_CONFIRM)
            return
        }

        view?.lockPinKeyboard()
        createPinCode(createdPin)
        if (authInteractor.getBiometricStatus() < BiometricStatus.AVAILABLE) {
            view?.navigateToMain()
        } else {
            view?.vibrate(VIBRATE_DURATION)
            enableBiometric()
        }
    }

    override fun onBackPressed() {
        createdPin = emptyString()
        when (pinMode) {
            PinMode.CREATE -> {
                if (!navigateBackOnBackPressed) {
                    view?.showUiKitSnackBar(messageResId = R.string.onboarding_lets_finish_last_step)
                    navigateBackOnBackPressed = true
                } else {
                    view?.navigateBack()
                }
            }
            PinMode.CONFIRM -> view?.showCreation()
        }
    }

    private fun enableBiometric() {
        try {
            val cipher = authInteractor.getPinEncodeCipher()
            analytics.logBioApproved()
            view?.showBiometricDialog(cipher.value)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to get cipher for biometrics")
            view?.navigateToMain()
        }
    }

    override fun createPin(biometricCipher: Cipher?) {
        launch {
            try {
                val encoderCipher = if (biometricCipher != null) EncodeCipher(biometricCipher) else null
                registerComplete(createdPin, encoderCipher)
                if (biometricCipher == null) analytics.logBioRejected()
                view?.navigateToMain()
            } catch (e: Throwable) {
                Timber.e(e, "Failed to create pin code")
                view?.showErrorMessage(R.string.error_general_message)
                view?.navigateToMain()
            }
        }
    }

    private fun createPinCode(pinCode: String) {
        view?.showLoading(true)
        launch {
            try {
                registerComplete(pinCode, null)
                view?.showUiKitSnackBar(messageResId = R.string.auth_create_wallet_pin_code_success)
            } catch (e: Throwable) {
                Timber.e(e, "Failed to create pin code")
                createdPin = emptyString()
                view?.showCreation()
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                view?.vibrate(VIBRATE_DURATION)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun registerComplete(pinCode: String, cipher: EncodeCipher?) {
        authInteractor.registerComplete(pinCode, cipher)
        authInteractor.finishSignUp()
        // TODO determine pin complexity
        adminAnalytics.logPinCreated(currentScreenName = analyticsInteractor.getCurrentScreenName())
    }
}

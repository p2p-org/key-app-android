package org.p2p.wallet.auth.ui.pin.create

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.analytics.OnBoardingAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import javax.crypto.Cipher

private const val VIBRATE_DURATION = 500L

class CreatePinPresenter(
    private val authInteractor: AuthInteractor,
    private val authLogoutInteractor: AuthLogoutInteractor,
    private val adminAnalytics: AdminAnalytics,
    private val analytics: OnBoardingAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor
) : BasePresenter<CreatePinContract.View>(),
    CreatePinContract.Presenter {

    private var createdPin = emptyString()

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

        if (authInteractor.getBiometricStatus() < BiometricStatus.AVAILABLE) {
            createPinCode(createdPin)
        } else {
            view?.onPinCreated()
        }
    }

    override fun enableBiometric() {
        try {
            val cipher = authInteractor.getPinEncodeCipher()
            analytics.logBioApproved()
            view?.showBiometricDialog(cipher.value)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to get cipher for biometrics")
            view?.showErrorMessage(R.string.error_general_message)
        }
    }

    override fun createPin(cipher: Cipher?) {
        launch {
            try {
                val encoderCipher = if (cipher != null) EncodeCipher(cipher) else null
                registerComplete(createdPin, encoderCipher)
                analytics.logBioRejected()
                view?.onAuthFinished()
            } catch (e: Throwable) {
                Timber.e(e, "Failed to create pin code")
                view?.showErrorMessage(R.string.error_general_message)
            }
        }
    }

    override fun clearUserData() {
        launch {
            authLogoutInteractor.onUserLogout()
            view?.navigateBack()
        }
    }

    private fun createPinCode(pinCode: String) {
        view?.showLoading(true)
        launch {
            try {
                registerComplete(pinCode, null)
                view?.onAuthFinished()
            } catch (e: Throwable) {
                Timber.e(e, "Failed to create pin code")
                createdPin = emptyString()
                view?.showCreation()
                view?.showErrorMessage(R.string.error_general_message)
                view?.vibrate(VIBRATE_DURATION)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun registerComplete(pinCode: String, cipher: EncodeCipher?) {
        authInteractor.registerComplete(pinCode, cipher)
        // TODO determine pin complexity
        adminAnalytics.logPinCreated(currentScreenName = analyticsInteractor.getCurrentScreenName())
    }
}

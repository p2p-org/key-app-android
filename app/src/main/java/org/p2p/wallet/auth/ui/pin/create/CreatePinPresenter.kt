package org.p2p.wallet.auth.ui.pin.create

import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import timber.log.Timber
import javax.crypto.Cipher

private const val VIBRATE_DURATION = 500L

class CreatePinPresenter(
    private val authInteractor: AuthInteractor,
    private val adminAnalytics: AdminAnalytics
) : BasePresenter<CreatePinContract.View>(),
    CreatePinContract.Presenter {

    private var createdPin = ""

    override fun setPinCode(pinCode: String) {
        if (createdPin.isEmpty()) {
            createdPin = pinCode
            view?.showConfirmation()
            return
        }

        if (pinCode != createdPin) {
            view?.showConfirmationError()
            view?.vibrate(VIBRATE_DURATION)
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
            view?.showBiometricDialog(cipher.value)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to get cipher for biometrics")
            view?.showErrorMessage(R.string.error_general_message)
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
                createdPin = ""
                view?.showCreation()
                view?.showErrorMessage(R.string.error_general_message)
                view?.vibrate(VIBRATE_DURATION)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun createPin(cipher: Cipher?) {
        launch {
            try {
                val encoderCipher = if (cipher != null) EncodeCipher(cipher) else null
                registerComplete(createdPin, encoderCipher)
                view?.onAuthFinished()
            } catch (e: Throwable) {
                Timber.e(e, "Failed to create pin code")
                view?.showErrorMessage(R.string.error_general_message)
            }
        }
    }

    private fun registerComplete(pinCode: String, cipher: EncodeCipher?) {
        authInteractor.registerComplete(pinCode, cipher)
        // TODO determine pin complexity
        adminAnalytics.logPinCreated(isPinComplex = false)
    }
}
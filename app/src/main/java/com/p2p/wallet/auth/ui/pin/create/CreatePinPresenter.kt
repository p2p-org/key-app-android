package com.p2p.wallet.auth.ui.pin.create

import com.p2p.wallet.R
import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.auth.model.BiometricStatus
import com.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.launch
import timber.log.Timber

private const val VIBRATE_DURATION = 500L

class CreatePinPresenter(
    private val authInteractor: AuthInteractor
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
            view?.navigateToBiometric(createdPin)
        }
    }

    private fun createPinCode(pinCode: String) {
        view?.showLoading(true)

        launch {
            try {
                authInteractor.registerComplete(pinCode, null)
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
}
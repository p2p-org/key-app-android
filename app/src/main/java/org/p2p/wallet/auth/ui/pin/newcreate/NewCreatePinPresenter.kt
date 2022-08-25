package org.p2p.wallet.auth.ui.pin.newcreate

import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString
import timber.log.Timber

private const val VIBRATE_DURATION = 500L

class NewCreatePinPresenter(
    private val authLogoutInteractor: AuthLogoutInteractor,
    private val adminAnalytics: AdminAnalytics,
    private val authInteractor: AuthInteractor,
    private val createWalletInteractor: CreateWalletInteractor
) : BasePresenter<NewCreatePinContract.View>(),
    NewCreatePinContract.Presenter {

    private var createdPin = emptyString()
    private var pinMode = PinMode.CREATE

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

        view?.apply {
            lockPinKeyboard()
            onPinCreated(createdPin)
            vibrate(VIBRATE_DURATION)
        }
    }

    override fun onBackPressed() {
        createdPin = emptyString()
        when (pinMode) {
            PinMode.CREATE -> view?.navigateBack()
            PinMode.CONFIRM -> view?.showCreation()
        }
    }

    override fun onPinCreated() {
        try {
            checkBiometricAvailability()
            // Clear pin in case of returning back
            createdPin = emptyString()
        } catch (e: Throwable) {
            Timber.e(e, "Failed to finish pin creation")
            view?.showErrorMessage(R.string.error_general_message)
        }
    }

    private fun checkBiometricAvailability() {
        if (authInteractor.getBiometricStatus() < BiometricStatus.AVAILABLE) {
            createWalletInteractor.finishAuthFlow()
            view?.navigateToMain()
        } else {
            view?.navigateToBiometrics(createdPin)
        }
    }
}

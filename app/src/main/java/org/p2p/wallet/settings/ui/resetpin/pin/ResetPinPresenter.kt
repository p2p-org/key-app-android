package org.p2p.wallet.settings.ui.resetpin.pin

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.auth.model.SignInResult
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString
import timber.log.Timber

private const val VIBRATE_DURATION = 500L
private const val PIN_ANIMATION_DURATION = 500L

class ResetPinPresenter(
    private val authInteractor: AuthInteractor,
    private val authLogoutInteractor: AuthLogoutInteractor
) : BasePresenter<ResetPinContract.View>(), ResetPinContract.Presenter {

    private var isCurrentPinConfirmed = false
    private var createdPin = emptyString()

    override fun setPinCode(pinCode: String) {
        if (!isCurrentPinConfirmed) {
            verifyPin(pinCode)
        } else {
            resetPin(pinCode)
        }
    }

    override fun logout() {
        launch {
            authLogoutInteractor.onUserLogout()
            // Onboarding fragment will figure out if device is saved or not
            // and do the navigation
            view?.navigateToOnboarding()
        }
    }

    private fun verifyPin(pinCode: String) {
        launch {
            try {
                onSignInResult(authInteractor.signInByPinCode(pinCode))
            } catch (e: Throwable) {
                Timber.e(e, "error checking current pin")
                view?.showIncorrectPinError()
                view?.vibrate(VIBRATE_DURATION)
            }
        }
    }

    private fun onSignInResult(result: SignInResult) {
        when (result) {
            is SignInResult.Success -> {
                launch {
                    isCurrentPinConfirmed = true
                    view?.showPinCorrect()
                    delay(PIN_ANIMATION_DURATION)
                    view?.showPinConfirmation()
                }
            }
            SignInResult.WrongPin -> {
                view?.showIncorrectPinError()
                view?.vibrate(VIBRATE_DURATION)
            }
        }
    }

    private fun resetPin(pinCode: String) {
        if (createdPin.isEmpty()) {
            createdPin = pinCode
            view?.showPinConfirmed()
            return
        }

        if (createdPin != pinCode) {
            view?.showIncorrectPinError()
            view?.showMessage(R.string.settings_item_pin_do_not_match)
            view?.vibrate(VIBRATE_DURATION)
            return
        }

        resetPinActually()
    }

    private fun resetPinActually() {
        launch {
            try {
                view?.showPinCorrect()
                delay(PIN_ANIMATION_DURATION)
                authInteractor.resetPin(createdPin)
                view?.showMessage(R.string.settings_item_pin_changed)
                view?.navigateBackToSettings()
            } catch (e: Throwable) {
                Timber.e(e, "Error during new pin reset")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                view?.vibrate(VIBRATE_DURATION)
            } finally {
                createdPin = emptyString()
            }
        }
    }
}

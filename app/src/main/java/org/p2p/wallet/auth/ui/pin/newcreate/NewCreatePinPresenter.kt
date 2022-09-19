package org.p2p.wallet.auth.ui.pin.newcreate

import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString
import timber.log.Timber
import kotlinx.coroutines.launch

private const val VIBRATE_DURATION = 500L

class NewCreatePinPresenter(
    private val adminAnalytics: AdminAnalytics,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val authInteractor: AuthInteractor,
    private val onboardingInteractor: OnboardingInteractor,
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
            if (onboardingInteractor.currentFlow == OnboardingFlow.CreateWallet) {
                onboardingAnalytics.logCreateWalletPinConfirm(OnboardingAnalytics.ConfirmPinResult.FAIL)
            } else {
                onboardingAnalytics.logRestoreWalletPinConfirm(OnboardingAnalytics.ConfirmPinResult.FAIL)
            }
            return
        }

        view?.lockPinKeyboard()
        createPinCode(createdPin)
        if (onboardingInteractor.currentFlow == OnboardingFlow.CreateWallet) {
            onboardingAnalytics.logCreateWalletPinConfirm(OnboardingAnalytics.ConfirmPinResult.SUCCESS)
        } else {
            onboardingAnalytics.logRestoreWalletPinConfirm(OnboardingAnalytics.ConfirmPinResult.SUCCESS)
        }
        if (authInteractor.getBiometricStatus() < BiometricStatus.AVAILABLE) {
            view?.navigateToMain()
        } else {
            view?.navigateToBiometrics(pinCode)
            view?.vibrate(VIBRATE_DURATION)
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

    private fun createPinCode(pinCode: String) {
        view?.showLoading(true)
        launch {
            try {
                registerComplete(pinCode, null)
                view?.navigateToMain()
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
        authInteractor.finishSignUp()
        // TODO determine pin complexity
        adminAnalytics.logPinCreated(currentScreenName = analyticsInteractor.getCurrentScreenName())
    }
}

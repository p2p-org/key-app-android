package org.p2p.wallet.auth.ui.pin.newcreate

import android.content.SharedPreferences
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.analytics.CreateWalletAnalytics
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.analytics.RestoreWalletAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.model.BiometricStatus
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.feature_toggles.toggles.remote.RegisterUsernameEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.KEY_IS_AUTH_BY_SEED_PHRASE
import org.p2p.wallet.utils.emptyString
import timber.log.Timber
import javax.crypto.Cipher
import kotlinx.coroutines.launch

private const val VIBRATE_DURATION = 500L

class NewCreatePinPresenter(
    private val analytics: OnboardingAnalytics,
    private val adminAnalytics: AdminAnalytics,
    private val createWalletAnalytics: CreateWalletAnalytics,
    private val restoreWalletAnalytics: RestoreWalletAnalytics,
    private val authInteractor: AuthInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val registerUsernameEnabledFeatureToggle: RegisterUsernameEnabledFeatureToggle,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val sharedPreferences: SharedPreferences
) : BasePresenter<NewCreatePinContract.View>(),
    NewCreatePinContract.Presenter {

    private var createdPin = emptyString()
    private var pinMode = PinMode.CREATE
    private var navigateBackOnBackPressed = false

    override fun attach(view: NewCreatePinContract.View) {
        super.attach(view)
        if (onboardingInteractor.currentFlow == OnboardingFlow.CreateWallet) {
            view.showUiKitSnackBar(messageResId = R.string.auth_create_wallet_introduction)
        }
    }

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
        if (onboardingInteractor.currentFlow == OnboardingFlow.CreateWallet) {
            createWalletAnalytics.logCreateWalletPinConfirmed()
        } else {
            restoreWalletAnalytics.logRestoreWalletPinConfirmed()
        }
        launch {
            if (authInteractor.getBiometricStatus() < BiometricStatus.AVAILABLE) {
                closeCreatePinFlow()
            } else {
                view?.vibrate(VIBRATE_DURATION)
                enableBiometric()
            }
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
            PinMode.CONFIRM -> {
                view?.showCreation()
            }
        }
    }

    private suspend fun enableBiometric() {
        try {
            val cipher = authInteractor.getPinEncodeCipher()
            analytics.logBioApproved()
            view?.showBiometricDialog(cipher.value)
        } catch (e: Throwable) {
            Timber.e(e, "Failed to get cipher for biometrics")
            closeCreatePinFlow()
        }
    }

    override fun createPin(biometricCipher: Cipher?) {
        launch {
            try {
                val encoderCipher = if (biometricCipher != null) EncodeCipher(biometricCipher) else null
                registerComplete(createdPin, encoderCipher)
                if (biometricCipher == null) analytics.logBioRejected()
                closeCreatePinFlow()
            } catch (e: Throwable) {
                Timber.e(e, "Failed to create pin code")
                view?.showErrorMessage(R.string.error_general_message)
                closeCreatePinFlow()
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

    private suspend fun closeCreatePinFlow() {
        // sometimes user can use seed phrase to login, we cant show item to him too
        val isUsernameAuthNotBySeedPhrase = !sharedPreferences.getBoolean(KEY_IS_AUTH_BY_SEED_PHRASE, false)
        val isUserCanRegisterUsername =
            registerUsernameEnabledFeatureToggle.isFeatureEnabled &&
                signUpDetailsStorage.getLastSignUpUserDetails() != null &&
                isUsernameAuthNotBySeedPhrase && signUpDetailsStorage.isSignUpInProcess()

        if (isUserCanRegisterUsername) {
            view?.navigateToRegisterUsername()
        } else {
            view?.navigateToMain(withAnimation = true)
        }
    }

    private suspend fun registerComplete(pinCode: String, cipher: EncodeCipher?) {
        authInteractor.registerComplete(pinCode, cipher)
        authInteractor.finishSignUp()
        // TODO determine pin complexity
        adminAnalytics.logPinCreated(currentScreenName = analyticsInteractor.getCurrentScreenName())
    }
}

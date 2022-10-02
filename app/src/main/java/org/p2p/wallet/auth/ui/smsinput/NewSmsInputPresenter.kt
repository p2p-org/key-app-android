package org.p2p.wallet.auth.ui.smsinput

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.model.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.repository.RestoreFailureState
import org.p2p.wallet.auth.repository.RestoreSuccessState
import org.p2p.wallet.auth.repository.RestoreUserExceptionHandler
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputContract.Presenter.SmsInputTimerState
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.removeWhiteSpaces
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

private const val DEFAULT_BLOCK_TIME_IN_MINUTES = 10

class NewSmsInputPresenter(
    private val createWalletInteractor: CreateWalletInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val resourcesProvider: ResourcesProvider,
    private val restoreUserExceptionHandler: RestoreUserExceptionHandler
) : BasePresenter<NewSmsInputContract.View>(), NewSmsInputContract.Presenter {

    override fun attach(view: NewSmsInputContract.View) {
        super.attach(view)
        // Determine which flow of onboard is active
        view.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
        val userPhoneNumber = when (onboardingInteractor.currentFlow) {
            is OnboardingFlow.CreateWallet -> createWalletInteractor.getUserPhoneNumber()
            is OnboardingFlow.RestoreWallet -> restoreWalletInteractor.getUserPhoneNumber()
        }
        userPhoneNumber?.let { view.initView(it) }
        connectToTimer()
    }

    // Start count down timer
    private fun connectToTimer() {
        createWalletInteractor.timer.let { timer ->
            launch {
                timer.collect { secondsBeforeResend ->
                    view?.renderSmsTimerState(SmsInputTimerState.ResendSmsNotReady(secondsBeforeResend))
                    if (secondsBeforeResend == 0) {
                        view?.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
                    }
                }
            }
        }
    }

    // Launch when sms code has been changed
    override fun onSmsInputChanged(smsCode: String) {
        if (isSmsCodeFormatValid(smsCode)) {
            view?.renderSmsFormatValid()
        } else {
            view?.renderSmsFormatInvalid()
        }
    }

    // Check for sms code, if is valid - start to CREATE OR RESTORE wallet
    override fun checkSmsValue(smsCode: String) {
        if (smsCode.isBlank()) {
            return
        }
        launch {
            val smsCodeRaw = smsCode.removeWhiteSpaces()
            when (onboardingInteractor.currentFlow) {
                is OnboardingFlow.CreateWallet -> finishCreatingWallet(smsCodeRaw)
                is OnboardingFlow.RestoreWallet -> finishRestoringCustomShare(smsCodeRaw)
            }
        }
    }

    override fun resendSms() {
        tryToResendSms()
        connectToTimer()
    }

    private fun handleGatewayError(error: GatewayServiceError) {
        when (error) {
            is GatewayServiceError.TooManyOtpRequests -> {
                Timber.e(error)
                val cooldownTtl = error.cooldownTtl
                val message = resourcesProvider.getString(R.string.error_too_often_otp_requests_message, cooldownTtl)
                view?.showUiKitSnackBar(message)
            }
            is GatewayServiceError.IncorrectOtpCode -> {
                Timber.i(error)
                view?.renderIncorrectSms()
            }
            is GatewayServiceError.TooManyRequests -> {
                Timber.i(error)
                view?.navigateToSmsInputBlocked(
                    error = GeneralErrorTimerScreenError.BLOCK_SMS_TOO_MANY_WRONG_ATTEMPTS,
                    timerLeftTime = DEFAULT_BLOCK_TIME_IN_MINUTES.minutes.inWholeSeconds
                )
            }
            is GatewayServiceError.CriticalServiceFailure -> {
                Timber.e(error)
                // TODO navigate to critical error screen
            }
        }
    }

    // Finish creating wallet
    private suspend fun finishCreatingWallet(smsCode: String) {
        try {
            view?.renderButtonLoading(isLoading = true)
            createWalletInteractor.finishCreatingWallet(smsCode)
            view?.navigateToPinCreate()
        } catch (gatewayError: GatewayServiceError) {
            handleGatewayError(gatewayError)
        } catch (error: Throwable) {
            Timber.e(error, "Checking sms value failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        } finally {
            view?.renderButtonLoading(isLoading = false)
        }
    }

    private suspend fun finishRestoringCustomShare(smsCode: String) {
        try {
            view?.renderButtonLoading(isLoading = true)
            restoreWalletInteractor.finishRestoreCustomShare(smsCode)
            val onboardFlow = onboardingInteractor.currentFlow as OnboardingFlow.RestoreWallet
            tryRestoreUser(onboardFlow)
        } catch (gatewayError: GatewayServiceError) {
            handleGatewayError(gatewayError)
        } catch (error: Throwable) {
            Timber.e(error, "Restoring user or custom share failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        } finally {
            view?.renderButtonLoading(isLoading = false)
        }
    }

    private suspend fun tryRestoreUser(flow: OnboardingFlow.RestoreWallet) {
        restoreWalletInteractor.tryRestoreUser(flow).takeIf {
            restoreWalletInteractor.isUserReadyToBeRestored(flow)
        }?.let { handleRestoreResult(it) }
    }

    private fun handleRestoreResult(result: RestoreUserResult) {
        val result = restoreUserExceptionHandler.handleRestoreResult(result)
        when (result) {
            is RestoreFailureState.TitleSubtitleError -> {
                view?.navigateToCriticalErrorScreen(result)
            }
            is RestoreSuccessState -> {
                view?.navigateToPinCreate()
            }
        }
    }

    private fun tryToResendSms() {
        launch {
            try {
                when (onboardingInteractor.currentFlow) {
                    is OnboardingFlow.RestoreWallet -> {
                        val userPhoneNumber = restoreWalletInteractor.getUserPhoneNumber()
                            ?: error("User phone number cannot be null")
                        restoreWalletInteractor.startRestoreCustomShare(
                            userPhoneNumber = userPhoneNumber,
                            isResend = true
                        )
                    }
                    is OnboardingFlow.CreateWallet -> {
                        val userPhoneNumber = createWalletInteractor.getUserPhoneNumber()
                            ?: error("User phone number cannot be null")
                        createWalletInteractor.startCreatingWallet(
                            userPhoneNumber = userPhoneNumber,
                            isResend = true
                        )
                    }
                }
            } catch (gatewayError: GatewayServiceError) {
                handleGatewayError(gatewayError)
            } catch (error: Throwable) {
                Timber.e(error, "Resending sms failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    override fun setGoogleSignInToken(userId: String, googleToken: String) {
        launch {
            restoreWalletInteractor.obtainTorusKey(googleToken, userId)
            restoreUserWithSocialPlusCustomShare()
        }
    }

    private suspend fun restoreUserWithSocialPlusCustomShare() {
        val restoreFlow = onboardingInteractor.currentFlow as OnboardingFlow.RestoreWallet.SocialPlusCustomShare
//        val restoreResult =
//            when (val result = restoreWalletInteractor.tryRestoreUser(restoreFlow)) {
//                is RestoreUserResult.RestoreSuccessful -> {
//                    restoreWalletInteractor.finishAuthFlow()
//                    view?.navigateToPinCreate()
//                }
//                is RestoreUserResult.RestoreFailed -> {
//                    Timber.e(result, "Restoring user social+custom share failed")
//                    view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
//                }
//            }
    }

    private fun isSmsCodeFormatValid(smsCode: String): Boolean {
        return smsCode.length == 6
    }
}

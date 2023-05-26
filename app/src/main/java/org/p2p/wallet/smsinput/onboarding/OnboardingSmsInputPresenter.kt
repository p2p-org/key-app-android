package org.p2p.wallet.smsinput.onboarding

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.CreateWalletAnalytics
import org.p2p.wallet.auth.analytics.RestoreWalletAnalytics
import org.p2p.wallet.auth.gateway.repository.model.PushServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreError
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.model.RestoreSuccessState
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.repository.GatewayServiceErrorHandler
import org.p2p.wallet.auth.repository.RestoreUserResultHandler
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputContract.Presenter.SmsInputTimerState
import org.p2p.wallet.utils.removeWhiteSpaces

class OnboardingSmsInputPresenter(
    private val createWalletInteractor: CreateWalletInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val createWalletAnalytics: CreateWalletAnalytics,
    private val restoreWalletAnalytics: RestoreWalletAnalytics,
    private val restoreUserResultHandler: RestoreUserResultHandler,
    private val gatewayServiceErrorHandler: GatewayServiceErrorHandler
) : BasePresenter<SmsInputContract.View>(), SmsInputContract.Presenter {

    override fun attach(view: SmsInputContract.View) {
        super.attach(view)
        // Determine which flow of onboard is active
        view.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
        val userPhoneNumber = when (onboardingInteractor.currentFlow) {
            is OnboardingFlow.CreateWallet -> {
                createWalletAnalytics.logCreateSmsInputScreenOpened()
                createWalletInteractor.getUserPhoneNumber()
            }
            is OnboardingFlow.RestoreWallet -> {
                restoreWalletAnalytics.logRestoreSmsInputScreenOpened()
                restoreWalletInteractor.getUserPhoneNumber()
            }
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

    private fun handleGatewayError(error: PushServiceError) {
        when (val gatewayHandledResult = gatewayServiceErrorHandler.handle(error)) {
            is GatewayHandledState.CriticalError -> {
                view?.navigateToGatewayErrorScreen(gatewayHandledResult)
            }
            GatewayHandledState.IncorrectOtpCodeError -> {
                view?.renderIncorrectSms()
            }
            is GatewayHandledState.TimerBlockError -> {
                view?.navigateToSmsInputBlocked(gatewayHandledResult.error, gatewayHandledResult.cooldownTtl)
            }
            is GatewayHandledState.TitleSubtitleError -> {
                view?.navigateToGatewayErrorScreen(gatewayHandledResult)
            }
            is GatewayHandledState.ToastError -> {
                view?.showUiKitSnackBar(gatewayHandledResult.message)
            }
            else -> {
                Timber.i("GatewayService error is not handled")
            }
        }
    }

    // Finish creating wallet
    private suspend fun finishCreatingWallet(smsCode: String) {
        try {
            view?.renderButtonLoading(isLoading = true)
            createWalletInteractor.finishCreatingWallet(smsCode)
            createWalletAnalytics.logSmsValidationResult(isSmsValid = true)

            view?.navigateNext()
        } catch (gatewayError: PushServiceError) {
            createWalletAnalytics.logSmsValidationResult(isSmsValid = false)
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
            restoreWalletAnalytics.logRestoreSmsValidationResult(isSmsValid = true)

            val onboardFlow = onboardingInteractor.currentFlow as OnboardingFlow.RestoreWallet
            tryRestoreUser(onboardFlow)
        } catch (gatewayError: PushServiceError) {
            restoreWalletAnalytics.logRestoreSmsValidationResult(isSmsValid = false)
            handleGatewayError(gatewayError)
        } catch (error: Throwable) {
            Timber.e(error, "Restoring user or custom share failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        } finally {
            view?.renderButtonLoading(isLoading = false)
        }
    }

    private suspend fun tryRestoreUser(flow: OnboardingFlow.RestoreWallet) {
        val result = restoreWalletInteractor.tryRestoreUser(flow)
        handleRestoreResult(result)
    }

    private suspend fun handleRestoreResult(result: RestoreUserResult) {
        when (val restoreResult = restoreUserResultHandler.handleRestoreResult(result)) {
            is RestoreFailureState.TitleSubtitleError -> {
                view?.navigateToRestoreErrorScreen(restoreResult)
            }
            is RestoreSuccessState -> {
                restoreWalletInteractor.finishAuthFlow()
                view?.navigateNext()
            }
            is RestoreFailureState.LogError -> {
                Timber.i("LogError for ${result::class.simpleName}")
                Timber.e(RestoreError(restoreResult.message))
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
            } catch (gatewayError: PushServiceError) {
                handleGatewayError(gatewayError)
            } catch (error: Throwable) {
                Timber.e(error, "Resending sms failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private fun isSmsCodeFormatValid(smsCode: String): Boolean {
        return smsCode.length == 6
    }
}

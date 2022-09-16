package org.p2p.wallet.auth.ui.smsinput

import kotlinx.coroutines.launch
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.ui.generalerror.GeneralErrorScreenError
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputContract.Presenter.SmsInputTimerState
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

private const val MAX_RESENT_CLICK_TRIES_COUNT = 5

class NewSmsInputPresenter(
    private val createWalletInteractor: CreateWalletInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor,
) : BasePresenter<NewSmsInputContract.View>(), NewSmsInputContract.Presenter {

    override fun attach(view: NewSmsInputContract.View) {
        super.attach(view)

        view.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
        val userPhoneNumber = when (onboardingInteractor.currentFlow) {
            is OnboardingFlow.CreateWallet -> createWalletInteractor.getUserPhoneNumber()
            is OnboardingFlow.RestoreWallet -> restoreWalletInteractor.getUserPhoneNumber()
        }
        userPhoneNumber?.let { view.initView(it) }
        connectToTimer()
    }

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

    override fun onSmsInputChanged(smsCode: String) {
        if (isSmsCodeFormatValid(smsCode)) {
            view?.renderSmsFormatValid()
        } else {
            view?.renderSmsFormatInvalid()
        }
    }

    override fun checkSmsValue(smsCode: String) {
        if (smsCode.isBlank()) {
            return
        }

        if (BuildConfig.DEBUG && smsCode == "111111") {
            view?.navigateToPinCreate()
            return
        }

        launch {
            when (onboardingInteractor.currentFlow) {
                is OnboardingFlow.CreateWallet -> finishCreatingWallet(smsCode)
                is OnboardingFlow.RestoreWallet -> finishRestoringCustomShare(smsCode)
            }
        }
    }

    private suspend fun finishCreatingWallet(smsCode: String) {
        try {
            view?.renderButtonLoading(isLoading = true)
            createWalletInteractor.finishCreatingWallet(smsCode)
            createWalletInteractor.finishAuthFlow()
            view?.navigateToPinCreate()
        } catch (tooOftenOtpRequests: GatewayServiceError.TooManyOtpRequests) {
            Timber.e(tooOftenOtpRequests)
            view?.showUiKitSnackBar(messageResId = R.string.error_too_often_otp_requests_message)
        } catch (incorrectSms: GatewayServiceError.IncorrectOtpCode) {
            Timber.i(incorrectSms)
            view?.renderIncorrectSms()
        } catch (tooManyRequests: GatewayServiceError.TooManyRequests) {
            Timber.i(tooManyRequests)
            view?.navigateToSmsInputBlocked(GeneralErrorTimerScreenError.BLOCK_SMS_TOO_MANY_WRONG_ATTEMPTS)
        } catch (serverError: GatewayServiceError.CriticalServiceFailure) {
            Timber.e(serverError)
            view?.navigateToCriticalErrorScreen(GeneralErrorScreenError.CriticalError(serverError.code))
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
            when (val currentFlow = onboardingInteractor.currentFlow) {
                is OnboardingFlow.RestoreWallet.DevicePlusCustomShare -> {
                    when (val flow = restoreWalletInteractor.tryRestoreUser(currentFlow)) {
                        RestoreUserResult.RestoreSuccessful -> {
                            view?.navigateToPinCreate()
                        }
                        RestoreUserResult.UserNotFound -> {
                            view?.navigateToCriticalErrorScreen(GeneralErrorScreenError.PhoneNumberDoesNotMatchError)
                        }
                        is RestoreUserResult.RestoreFailed -> {
                            view?.showErrorMessage(messageResId = R.string.error_general_message)
                        }
                    }
                }
                is OnboardingFlow.RestoreWallet.SocialPlusCustomShare -> {
                    if (restoreWalletInteractor.isUserReadyToBeRestored(currentFlow)) {
                        restoreUserWithShares()
                    } else {
                        // no social share, requesting now
                        view?.requestGoogleSignIn()
                    }
                }
            }
        } catch (error: Throwable) {
            Timber.e(error, "Checking sms value failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        } finally {
            view?.renderButtonLoading(isLoading = false)
        }
    }

    override fun resendSms() {
        if (createWalletInteractor.resetCount >= MAX_RESENT_CLICK_TRIES_COUNT) {
            view?.navigateToSmsInputBlocked(GeneralErrorTimerScreenError.BLOCK_SMS_RETRY_BUTTON_TRIES_EXCEEDED)
        } else {
            launch {
                try {
                    view?.renderButtonLoading(isLoading = true)

                    when (onboardingInteractor.currentFlow) {
                        is OnboardingFlow.RestoreWallet -> {
                            val userPhoneNumber = restoreWalletInteractor.getUserPhoneNumber()
                                ?: throw IllegalStateException("User phone number cannot be null")
                            restoreWalletInteractor.startRestoreCustomShare(
                                userPhoneNumber = userPhoneNumber,
                                isResend = true
                            )
                        }
                        is OnboardingFlow.CreateWallet -> {
                            val userPhoneNumber = createWalletInteractor.getUserPhoneNumber()
                                ?: throw IllegalStateException("User phone number cannot be null")
                            createWalletInteractor.startCreatingWallet(
                                userPhoneNumber = userPhoneNumber,
                                isResend = true
                            )
                        }
                    }
                } catch (tooOftenOtpRequests: GatewayServiceError.TooManyOtpRequests) {
                    Timber.e(tooOftenOtpRequests)
                    view?.showUiKitSnackBar(messageResId = R.string.error_too_often_otp_requests_message)
                } catch (serverError: GatewayServiceError.CriticalServiceFailure) {
                    Timber.e(serverError)
                    view?.navigateToCriticalErrorScreen(GeneralErrorScreenError.CriticalError(serverError.code))
                } catch (error: Throwable) {
                    Timber.e(error, "Resending sms failed")
                    view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                } finally {
                    view?.renderButtonLoading(isLoading = false)
                }
            }
            connectToTimer()
        }
    }

    override fun setGoogleSignInToken(userId: String, googleToken: String) {
        launch {
            restoreWalletInteractor.restoreSocialShare(googleToken, userId)
            restoreUserWithShares()
        }
    }

    private suspend fun restoreUserWithShares() {
        val restoreFlow = onboardingInteractor.currentFlow as OnboardingFlow.RestoreWallet
        when (val result = restoreWalletInteractor.tryRestoreUser(restoreFlow)) {
            is RestoreUserResult.RestoreSuccessful -> {
                restoreWalletInteractor.finishAuthFlow()
                view?.navigateToPinCreate()
            }
            is RestoreUserResult.RestoreFailed -> {
                Timber.e(result)
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private fun isSmsCodeFormatValid(smsCode: String): Boolean {
        return smsCode.length == 6
    }
}

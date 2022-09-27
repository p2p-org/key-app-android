package org.p2p.wallet.auth.ui.smsinput

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.model.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.ui.generalerror.GeneralErrorScreenError
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputContract.Presenter.SmsInputTimerState
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.removeWhiteSpaces
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val DEFAULT_BLOCK_TIME_IN_MINUTES = 10

class NewSmsInputPresenter(
    private val createWalletInteractor: CreateWalletInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val resourcesProvider: ResourcesProvider
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

        launch {
            val smsCodeRaw = smsCode.removeWhiteSpaces()
            when (onboardingInteractor.currentFlow) {
                is OnboardingFlow.CreateWallet -> finishCreatingWallet(smsCodeRaw)
                is OnboardingFlow.RestoreWallet -> finishRestoringCustomShare(smsCodeRaw)
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
            val cooldownTtl = tooOftenOtpRequests.cooldownTtl
            val message = resourcesProvider.getString(R.string.error_too_often_otp_requests_message, cooldownTtl)
            view?.showUiKitSnackBar(message)
        } catch (incorrectSms: GatewayServiceError.IncorrectOtpCode) {
            Timber.i(incorrectSms)
            view?.renderIncorrectSms()
        } catch (tooManyRequests: GatewayServiceError.TooManyRequests) {
            Timber.i(tooManyRequests)
            view?.navigateToSmsInputBlocked(
                error = GeneralErrorTimerScreenError.BLOCK_SMS_TOO_MANY_WRONG_ATTEMPTS,
                timerLeftTime = DEFAULT_BLOCK_TIME_IN_MINUTES.minutes.inWholeSeconds
            )
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
            restoreUserWithShares()
        } catch (tooOftenOtpRequests: GatewayServiceError.TooManyOtpRequests) {
            Timber.e(tooOftenOtpRequests)
            val cooldownTtl = tooOftenOtpRequests.cooldownTtl
            val message = resourcesProvider.getString(R.string.error_too_often_otp_requests_message, cooldownTtl)
            view?.showUiKitSnackBar(message)
        } catch (incorrectSms: GatewayServiceError.IncorrectOtpCode) {
            Timber.i(incorrectSms)
            view?.renderIncorrectSms()
        } catch (tooManyRequests: GatewayServiceError.TooManyRequests) {
            Timber.i(tooManyRequests)
            view?.navigateToSmsInputBlocked(
                error = GeneralErrorTimerScreenError.BLOCK_SMS_TOO_MANY_WRONG_ATTEMPTS,
                timerLeftTime = tooManyRequests.cooldownTtl.seconds.inWholeSeconds
            )
        } catch (serverError: GatewayServiceError.CriticalServiceFailure) {
            Timber.e(serverError)
            view?.navigateToCriticalErrorScreen(GeneralErrorScreenError.CriticalError(serverError.code))
        } catch (error: Throwable) {
            Timber.e(error, "Restoring user or custom share failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        } finally {
            view?.renderButtonLoading(isLoading = false)
        }
    }

    private suspend fun restoreUserWithShares() {
        when (onboardingInteractor.currentFlow) {
            is OnboardingFlow.RestoreWallet.DevicePlusCustomShare -> {
                restoreUserWithDevicePlusCustomShare()
            }
            is OnboardingFlow.RestoreWallet.SocialPlusCustomShare -> {
                tryRestoreUserWithSocialPlusCustomShare()
            }
        }
    }

    private suspend fun restoreUserWithDevicePlusCustomShare() {
        when (val result = restoreWalletInteractor.tryRestoreUser(OnboardingFlow.RestoreWallet.DevicePlusCustomShare)) {
            RestoreUserResult.RestoreSuccessful -> {
                restoreWalletInteractor.finishAuthFlow()
                view?.navigateToPinCreate()
            }
            RestoreUserResult.SharesDoNotMatch -> {
                view?.navigateToCriticalErrorScreen(GeneralErrorScreenError.SharesDoNotMatchError)
            }
            RestoreUserResult.UserNotFound -> {
                view?.navigateToCriticalErrorScreen(GeneralErrorScreenError.PhoneNumberDoesNotMatchError)
            }
            is RestoreUserResult.RestoreFailed -> {
                Timber.e(result, "Restoring user device+custom share failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            is RestoreUserResult.DeviceShareNotFound -> {
                Timber.e("Restoring user device+ custom, device share not found")
                view?.navigateToCriticalErrorScreen(GeneralErrorScreenError.DeviceShareNotFound)
            }
        }
    }

    private suspend fun tryRestoreUserWithSocialPlusCustomShare() {
        if (restoreWalletInteractor.isUserReadyToBeRestored(OnboardingFlow.RestoreWallet.SocialPlusCustomShare)) {
            restoreUserWithSocialPlusCustomShare()
        } else {
            // no social share, requesting now
            view?.requestGoogleSignIn()
        }
    }

    override fun resendSms() {
        tryToResendSms()
        connectToTimer()
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
            } catch (tooOftenOtpRequests: GatewayServiceError.TooManyOtpRequests) {
                Timber.e(tooOftenOtpRequests)
                val message = resourcesProvider.getString(
                    R.string.error_too_often_otp_requests_message,
                    tooOftenOtpRequests.cooldownTtl
                )
                view?.showUiKitSnackBar(message)
            } catch (tooManyRequests: GatewayServiceError.TooManyRequests) {
                Timber.e(tooManyRequests)
                view?.navigateToSmsInputBlocked(
                    error = GeneralErrorTimerScreenError.BLOCK_SMS_RETRY_BUTTON_TRIES_EXCEEDED,
                    timerLeftTime = tooManyRequests.cooldownTtl.seconds.inWholeSeconds
                )
            } catch (serverError: GatewayServiceError.CriticalServiceFailure) {
                Timber.e(serverError)
                view?.navigateToCriticalErrorScreen(GeneralErrorScreenError.CriticalError(serverError.code))
            } catch (error: Throwable) {
                Timber.e(error, "Resending sms failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    override fun setGoogleSignInToken(userId: String, googleToken: String) {
        launch {
            restoreWalletInteractor.restoreSocialShare(googleToken, userId)
            restoreUserWithSocialPlusCustomShare()
        }
    }

    private suspend fun restoreUserWithSocialPlusCustomShare() {
        val restoreFlow = onboardingInteractor.currentFlow as OnboardingFlow.RestoreWallet.SocialPlusCustomShare
        when (val result = restoreWalletInteractor.tryRestoreUser(restoreFlow)) {
            is RestoreUserResult.RestoreSuccessful -> {
                restoreWalletInteractor.finishAuthFlow()
                view?.navigateToPinCreate()
            }
            is RestoreUserResult.RestoreFailed -> {
                Timber.e(result, "Restoring user social+custom share failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private fun isSmsCodeFormatValid(smsCode: String): Boolean {
        return smsCode.length == 6
    }
}

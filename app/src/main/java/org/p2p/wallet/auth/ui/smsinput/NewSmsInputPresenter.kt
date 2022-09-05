package org.p2p.wallet.auth.ui.smsinput

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.auth.ui.smsinput.NewSmsInputContract.Presenter.SmsInputTimerState
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

private const val MAX_RESENT_CLICK_TRIES_COUNT = 5

class NewSmsInputPresenter(
    private val createWalletInteractor: CreateWalletInteractor,
    private val repository: SignUpFlowDataLocalRepository
) : BasePresenter<NewSmsInputContract.View>(), NewSmsInputContract.Presenter {

    private var timerFlow: Job? = null

    private var smsResendCount = 0

    private var smsIncorrectTries = 0

    override fun attach(view: NewSmsInputContract.View) {
        super.attach(view)

        view.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
        view.initView(repository.userPhoneNumber.orEmpty())
    }

    override fun onSmsInputChanged(smsCode: String) {
        if (isSmsCodeFormatValid(smsCode)) {
            view?.renderSmsFormatValid()
            checkSmsValue(smsCode)
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
            try {
                view?.renderButtonLoading(isLoading = true)
                createWalletInteractor.finishCreatingWallet(smsCode)
                createWalletInteractor.finishAuthFlow()
                view?.navigateToPinCreate()
            } catch (incorrectSms: GatewayServiceError.IncorrectOtpCode) {
                Timber.i(incorrectSms)
                view?.renderIncorrectSms()
            } catch (tooManyRequests: GatewayServiceError.TooManyRequests) {
                Timber.i(tooManyRequests)
                view?.navigateToSmsInputBlocked(GeneralErrorTimerScreenError.BLOCK_SMS_TOO_MANY_WRONG_ATTEMPTS)
            } catch (serverError: GatewayServiceError.CriticalServiceFailure) {
                Timber.e(serverError)
                view?.navigateToCriticalErrorScreen(serverError.code)
            } catch (error: Throwable) {
                Timber.e(error, "Checking sms value failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            } finally {
                view?.renderButtonLoading(isLoading = false)
            }
        }
    }

    override fun resendSms() {
        launch {
            try {
                if (++smsResendCount >= MAX_RESENT_CLICK_TRIES_COUNT && smsIncorrectTries == 0) {
                    view?.navigateToSmsInputBlocked(GeneralErrorTimerScreenError.BLOCK_SMS_RETRY_BUTTON_TRIES_EXCEEDED)
                    return@launch
                }
                timerFlow?.cancel()
                timerFlow = createSmsInputTimer(timerSeconds = 5 * smsResendCount)
                    .toResendSmsInputTimer()
                    .launchIn(this)

                view?.renderButtonLoading(isLoading = true)

                createWalletInteractor.startCreatingWallet(repository.userPhoneNumber.orEmpty())
            } catch (serverError: GatewayServiceError.CriticalServiceFailure) {
                Timber.e(serverError)
                view?.navigateToCriticalErrorScreen(serverError.code)
            } catch (error: Throwable) {
                Timber.e(error, "Resending sms failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            } finally {
                view?.renderButtonLoading(isLoading = false)
            }
        }
    }

    private fun Flow<Int>.toResendSmsInputTimer(): Flow<Int> {
        return onEach { secondsBeforeResend ->
            view?.renderSmsTimerState(SmsInputTimerState.ResendSmsNotReady(secondsBeforeResend))
            if (secondsBeforeResend == 0) {
                view?.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
            }
        }
    }

    private fun Flow<Int>.toRequestsOverflowSmsInputTimer(): Flow<Int> {
        return onEach { secondsBeforeResend ->
            view?.renderSmsTimerState(SmsInputTimerState.SmsValidationBlocked(secondsBeforeResend))
            if (secondsBeforeResend == 0) {
                view?.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
            }
        }
    }

    private fun isSmsCodeFormatValid(smsCode: String): Boolean {
        return smsCode.length == 6
    }

    private fun createSmsInputTimer(timerSeconds: Int): Flow<Int> =
        (timerSeconds downTo 0)
            .asSequence()
            .asFlow()
            .onEach { delay(1.seconds.inWholeMilliseconds) }
}

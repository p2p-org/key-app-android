package org.p2p.wallet.auth.ui.smsinput

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.gateway.repository.GatewayServiceError
import org.p2p.wallet.auth.interactor.CreateWalletInteractor
import org.p2p.wallet.auth.interactor.SignUpFlowDataCache
import org.p2p.wallet.auth.ui.smsinput.NewAuthSmsInputContract.Presenter.SmsInputTimerState
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

class NewSmsInputPresenter(
    private val createWalletInteractor: CreateWalletInteractor,
    private val cache: SignUpFlowDataCache
) : BasePresenter<NewAuthSmsInputContract.View>(), NewAuthSmsInputContract.Presenter {

    private var timerFlow: Job? = null

    private var smsResendCount = 0

    private var smsIncorrectTries = 0

    override fun attach(view: NewAuthSmsInputContract.View) {
        super.attach(view)

        view.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
        view.initView(cache.userPhoneNumber.orEmpty())
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

        launch {
            try {
                view?.renderButtonLoading(isLoading = true)

                createWalletInteractor.finishCreatingWallet(smsCode)
                view?.navigateToPinCreate()
            } catch (incorrectSms: GatewayServiceError.IncorrectOtpCode) {
                Timber.i(incorrectSms)
                if (smsIncorrectTries > 5) {
                    view?.navigateToSmsInputBlocked()
                } else {
                    view?.renderIncorrectSms()
                }
            } catch (tooManyRequests: GatewayServiceError.TooManyRequests) {
                Timber.i(tooManyRequests)
                timerFlow?.cancel()
                timerFlow = createSmsInputTimer(timerSeconds = 5)
                    .toRequestsOverflowSmsInputTimer()
                    .launchIn(this)
            } catch (serverError: GatewayServiceError.TemporaryFailure) {
                Timber.i(serverError)
                view?.showErrorMessage(serverError)
            } finally {
                view?.renderButtonLoading(isLoading = false)
            }
        }
    }

    override fun resendSms() {
        launch {
            try {
                smsResendCount++
                timerFlow?.cancel()
                timerFlow = createSmsInputTimer(timerSeconds = 5 * smsResendCount)
                    .toResendSmsInputTimer()
                    .launchIn(this)

                view?.renderButtonLoading(isLoading = true)

                createWalletInteractor.startCreatingWallet(cache.userPhoneNumber.orEmpty())
            } catch (serverError: GatewayServiceError.TemporaryFailure) {
                Timber.i(serverError)
                view?.showErrorMessage(serverError)
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

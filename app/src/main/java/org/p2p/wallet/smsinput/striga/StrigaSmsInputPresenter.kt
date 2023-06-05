package org.p2p.wallet.smsinput.striga

import timber.log.Timber
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputContract.Presenter.SmsInputTimerState
import org.p2p.wallet.striga.model.StrigaApiErrorCode
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.utils.removeWhiteSpaces

class StrigaSmsInputPresenter(
    private val interactor: StrigaSmsInputInteractor
) : BasePresenter<SmsInputContract.View>(), SmsInputContract.Presenter {

    override fun attach(view: SmsInputContract.View) {
        super.attach(view)
        view.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
    }

    override fun onSmsInputChanged(smsCode: String) {
        // same logic in onboarding, need to reuse
        if (isSmsCodeFormatValid(smsCode)) {
            view?.renderSmsFormatValid()
        } else {
            view?.renderSmsFormatInvalid()
        }
    }

    private fun isSmsCodeFormatValid(smsCode: String): Boolean = smsCode.length == 6

    override fun checkSmsValue(smsCode: String) {
        if (smsCode.isBlank()) return
        view?.renderButtonLoading(isLoading = true)

        // same logic in onboarding
        val smsCodeRaw = smsCode.removeWhiteSpaces()
        launch {
            when (val result = interactor.validateSms(smsCodeRaw)) {
                is StrigaDataLayerResult.Success -> view?.navigateNext()
                is StrigaDataLayerResult.Failure -> handleError(result.error)
            }
            view?.renderButtonLoading(isLoading = false)
        }
    }

    private fun handleError(error: StrigaDataLayerError) {
        when (error) {
            is StrigaDataLayerError.ApiServiceError -> {
                handleApiError(error.errorCode)
            }
            is StrigaDataLayerError.ApiServiceUnavailable, is StrigaDataLayerError.InternalError -> {
                Timber.e(error, "Sms verification failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private fun handleApiError(code: StrigaApiErrorCode) {
        when (code) {
            StrigaApiErrorCode.MOBILE_ALREADY_VERIFIED -> {
                // separate screen
            }
            StrigaApiErrorCode.INVALID_VERIFICATION_CODE -> {
                view?.renderIncorrectSms()
            }
            StrigaApiErrorCode.EXCEEDED_VERIFICATION_ATTEMPTS -> {
                view?.renderSmsTimerState(
                    SmsInputTimerState.SmsValidationBlocked(1.days.inWholeSeconds.toInt())
                )
            }
            StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT -> {
                view?.renderSmsTimerState(
                    SmsInputTimerState.SmsValidationResendButtonExceeded(1.days.inWholeSeconds.toInt())
                )
            }
            else -> {
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    override fun resendSms() {
        launch {
            when (val result = interactor.resendSms()) {
                is StrigaDataLayerResult.Success -> {
                    view?.renderSmsTimerState(
                        SmsInputTimerState.ResendSmsNotReady(5)
                    )
                }
                is StrigaDataLayerResult.Failure -> {
                    handleError(result.error)
                }
            }
            view?.renderButtonLoading(isLoading = false)
        }
    }
}

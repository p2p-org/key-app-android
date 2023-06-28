package org.p2p.wallet.smsinput.striga

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.smsinput.SmsInputContract
import org.p2p.wallet.smsinput.SmsInputContract.Presenter.SmsInputTimerState
import org.p2p.wallet.striga.model.StrigaApiErrorCode
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.utils.removeWhiteSpaces

class StrigaSmsInputPresenter(
    private val interactor: StrigaSmsInputInteractor,
) : BasePresenter<SmsInputContract.View>(), SmsInputContract.Presenter {

    override fun firstAttach() {
        super.firstAttach()
        checkForExceededLimits()
        resendSmsIfNeeded()
    }

    override fun attach(view: SmsInputContract.View) {
        super.attach(view)
        view.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
        initPhoneNumber()
        connectToTimer()
    }

    override fun onSmsInputChanged(smsCode: String) {
        // same logic in onboarding, need to reuse
        if (isSmsCodeFormatValid(smsCode)) {
            view?.renderSmsFormatValid()
        } else {
            view?.renderSmsFormatInvalid()
        }
    }

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

    private fun resendSmsIfNeeded() {
        if (interactor.isTimerNotActive) {
            resendSms()
        }
    }

    override fun resendSms() {
        launch {
            when (val result = interactor.resendSms()) {
                is StrigaDataLayerResult.Success -> {
                    // we don't have a timer yet
                    view?.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
                }
                is StrigaDataLayerResult.Failure -> {
                    handleError(result.error)
                }
            }
            view?.renderButtonLoading(isLoading = false)
        }
    }

    private fun checkForExceededLimits() {
        interactor.getExceededLimitsErrorIfPresent()?.let {
            if (it.error is StrigaDataLayerError.ApiServiceError) {
                handleApiError(it.error)
            }
        }
    }

    private fun initPhoneNumber() {
        launch {
            try {
                val phoneNumber = interactor.getUserPhoneCodeToPhoneNumber()
                view?.initView(PhoneNumber(formattedValue = phoneNumber.formattedPhoneNumberByMask))
            } catch (initViewError: Throwable) {
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                Timber.e(initViewError, "failed to init view for sms input")
            }
        }
    }

    private fun connectToTimer() {
        launch {
            interactor.timer.collect { secondsBeforeResend ->
                view?.renderSmsTimerState(SmsInputTimerState.ResendSmsNotReady(secondsBeforeResend))
                if (secondsBeforeResend == 0) {
                    view?.renderSmsTimerState(SmsInputTimerState.ResendSmsReady)
                }
            }
        }
    }

    private fun isSmsCodeFormatValid(smsCode: String): Boolean = smsCode.length == 6

    private fun handleError(error: StrigaDataLayerError) {
        when (error) {
            is StrigaDataLayerError.ApiServiceError -> {
                handleApiError(error)
            }
            is StrigaDataLayerError.ApiServiceUnavailable, is StrigaDataLayerError.InternalError -> {
                Timber.e(error, "Sms verification failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private fun handleApiError(apiServiceError: StrigaDataLayerError.ApiServiceError) {
        when (apiServiceError.errorCode) {
            StrigaApiErrorCode.INVALID_VERIFICATION_CODE -> {
                view?.renderIncorrectSms()
            }
            StrigaApiErrorCode.EXCEEDED_VERIFICATION_ATTEMPTS -> {
                view?.navigateToExceededConfirmationAttempts()
            }
            StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT -> {
                view?.navigateToExceededDailyResendSmsLimit()
            }
            else -> {
                Timber.e(apiServiceError, "Unknown code met when handling error for sms input")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }
}

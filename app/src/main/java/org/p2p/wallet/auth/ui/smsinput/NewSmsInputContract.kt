package org.p2p.wallet.auth.ui.smsinput

import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewSmsInputContract {
    interface View : MvpView {
        fun initView(userPhoneNumber: String)

        fun renderSmsFormatValid()
        fun renderSmsFormatInvalid()
        fun renderIncorrectSms()

        fun renderSmsTimerState(timerState: Presenter.SmsInputTimerState)
        fun renderButtonLoading(isLoading: Boolean)

        fun navigateToPinCreate()
        fun navigateToSmsInputBlocked(error: GeneralErrorTimerScreenError)
        fun navigateToCriticalErrorScreen(errorCode: Int)
    }

    interface Presenter : MvpPresenter<View> {
        sealed interface SmsInputTimerState {
            class ResendSmsNotReady(val secondsBeforeResend: Int) : SmsInputTimerState
            object ResendSmsReady : SmsInputTimerState

            class SmsValidationBlocked(val secondsBeforeUnlock: Int) : SmsInputTimerState
            class SmsValidationResendButtonExceeded(val secondsBeforeUnlock: Int) : SmsInputTimerState
        }

        fun onSmsInputChanged(smsCode: String)
        fun checkSmsValue(smsCode: String)
        fun resendSms()
    }
}

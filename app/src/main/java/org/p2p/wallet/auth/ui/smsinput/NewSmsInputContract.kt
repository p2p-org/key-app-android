package org.p2p.wallet.auth.ui.smsinput

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
        fun navigateToSmsInputBlocked()
        fun navigateToCriticalErrorScreen(errorCode: Int)
    }

    interface Presenter : MvpPresenter<View> {
        sealed interface SmsInputTimerState {
            class ResendSmsNotReady(val secondsBeforeResend: Int) : SmsInputTimerState
            object ResendSmsReady : SmsInputTimerState

            class SmsValidationBlocked(val secondsBeforeUnblock: Int) : SmsInputTimerState
        }

        fun onSmsInputChanged(smsCode: String)
        fun checkSmsValue(smsCode: String)
        fun resendSms()
    }
}

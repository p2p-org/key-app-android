package org.p2p.wallet.auth.ui.smsinput

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewAuthSmsInputContract {
    interface View : MvpView {
        fun initView(userPhoneNumber: String)

        fun renderSmsFormatValid()
        fun renderSmsFormatInvalid()
        fun renderIncorrectSms()

        fun renderSmsTimerState(timerState: Presenter.SmsInputTimerState)

        fun navigateToPinCreate()
        fun renderButtonLoading(isLoading: Boolean)
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

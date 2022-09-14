package org.p2p.wallet.auth.ui.smsinput

import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.ui.generalerror.GeneralErrorScreenError
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewSmsInputContract {
    interface View : MvpView {
        fun initView(userPhoneNumber: PhoneNumber)

        fun renderSmsFormatValid()
        fun renderSmsFormatInvalid()
        fun renderIncorrectSms()

        fun renderSmsTimerState(timerState: Presenter.SmsInputTimerState)
        fun renderButtonLoading(isLoading: Boolean)

        fun navigateToPinCreate()
        fun navigateToSmsInputBlocked(error: GeneralErrorTimerScreenError)
        fun navigateToCriticalErrorScreen(screenError: GeneralErrorScreenError)
        fun requestGoogleSignIn()
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
        fun setGoogleSignInToken(userId: String, googleToken: String)
    }
}

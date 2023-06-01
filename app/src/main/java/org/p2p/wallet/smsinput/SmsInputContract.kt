package org.p2p.wallet.smsinput

import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.ui.generalerror.timer.GeneralErrorTimerScreenError
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SmsInputContract {
    interface View : MvpView {
        fun initView(userPhoneNumber: PhoneNumber)

        fun renderSmsFormatValid()
        fun renderSmsFormatInvalid()
        fun renderIncorrectSms()

        fun renderSmsTimerState(timerState: Presenter.SmsInputTimerState)
        fun renderButtonLoading(isLoading: Boolean)

        fun navigateNext()
        fun navigateToSmsInputBlocked(error: GeneralErrorTimerScreenError, timerLeftTime: Long)
        fun navigateToGatewayErrorScreen(handledState: GatewayHandledState)
        fun navigateToRestoreErrorScreen(handledState: RestoreFailureState.TitleSubtitleError)
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

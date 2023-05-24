package org.p2p.wallet.smsinput.striga

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.smsinput.SmsInputContract

class StrigaSmsInputPresenter() : BasePresenter<SmsInputContract.View>(), SmsInputContract.Presenter {

    override fun attach(view: SmsInputContract.View) {
        super.attach(view)
        view.renderSmsTimerState(SmsInputContract.Presenter.SmsInputTimerState.ResendSmsReady)
    }

    override fun onSmsInputChanged(smsCode: String) {
    }

    override fun checkSmsValue(smsCode: String) {
    }

    override fun resendSms() {
    }
}

package org.p2p.wallet.auth.ui.pin.select

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import javax.crypto.Cipher

interface TouchIdContract {

    interface View : MvpView {
        fun showBiometricDialog(cipher: Cipher)
        fun onAuthFinished()
    }

    interface Presenter : MvpPresenter<View> {
        fun enableBiometric()
        fun finishAuthorization()
        fun createPin(pinCode: String, cipher: Cipher?)
    }
}

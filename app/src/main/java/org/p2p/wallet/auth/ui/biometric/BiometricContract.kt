package org.p2p.wallet.auth.ui.biometric

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import javax.crypto.Cipher

interface BiometricContract {

    interface View : MvpView {
        fun showBiometricDialog(cipher: Cipher)
        fun onAuthFinished()
    }

    interface Presenter : MvpPresenter<View> {
        fun enableBiometric()
        fun createPin(pinCode: String, cipher: Cipher?)
    }
}
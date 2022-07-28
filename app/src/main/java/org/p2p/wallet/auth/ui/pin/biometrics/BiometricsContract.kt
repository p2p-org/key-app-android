package org.p2p.wallet.auth.ui.pin.biometrics

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import javax.crypto.Cipher

interface BiometricsContract {

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

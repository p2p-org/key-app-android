package org.p2p.wallet.settings.ui.security

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import javax.crypto.Cipher

interface SecurityContract {

    interface View : MvpView {
        fun showBiometricActive(isActive: Boolean)
        fun showBiometricEnabled(isEnabled: Boolean)
        fun confirmBiometrics(cipher: Cipher)
        fun showConfirmationEnabled(isEnabled: Boolean)
        fun showConfirmationActive(isActive: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun setBiometricEnabled(isEnabled: Boolean)
        fun onBiometricsConfirmed(cipher: Cipher)
        fun onConfirmationStateChanged(isEnabled: Boolean)
    }
}

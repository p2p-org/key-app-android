package org.p2p.wallet.settings.ui.security

import org.p2p.wallet.auth.model.BiometricType
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import javax.crypto.Cipher

interface SecurityContract {

    interface View : MvpView {
        fun showBiometricData(type: BiometricType)
        fun showBiometricActive(isActive: Boolean)
        fun showBiometricEnabled(isEnabled: Boolean)
        fun confirmBiometrics(cipher: Cipher)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadBiometricType()
        fun setBiometricEnabled(isEnabled: Boolean)
        fun onBiometricsConfirmed(cipher: Cipher)
    }
}
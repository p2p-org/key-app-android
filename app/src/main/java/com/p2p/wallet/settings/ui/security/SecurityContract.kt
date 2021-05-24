package com.p2p.wallet.settings.ui.security

import com.p2p.wallet.auth.model.BiometricType
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import javax.crypto.Cipher

interface SecurityContract {

    interface View : MvpView {
        fun showBiometricData(type: BiometricType)
        fun showBiometricActive(isActive: Boolean)
        fun confirmBiometrics(cipher: Cipher)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadBiometricType()
        fun setBiometricEnabled(isEnabled: Boolean)
        fun onBiometricsConfirmed(cipher: Cipher)
    }
}
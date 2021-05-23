package com.p2p.wallet.settings.ui.reset

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import javax.crypto.Cipher

interface ResetPinContract {

    interface View : MvpView {
        fun showLoading(isLoading: Boolean)
        fun showCurrentPinIncorrectError(attemptsLeft: Int)
        fun showEnterNewPin()
        fun showConfirmationError()
        fun showConfirmNewPin()
        fun showWalletLocked(seconds: Long)
        fun showWalletUnlocked()
        fun showResetSuccess()
        fun vibrate(duration: Long)
        fun showBiometricDialog(cipher: Cipher)
        fun clearPin()
    }

    interface Presenter : MvpPresenter<View> {
        fun setPinCode(pinCode: String)
        fun resetPinWithoutBiometrics()
        fun resetPinWithBiometrics(cipher: Cipher)
        fun logout()
    }
}
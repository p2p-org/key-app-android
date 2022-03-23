package org.p2p.wallet.settings.ui.reset

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import javax.crypto.Cipher

interface ResetPinContract {

    interface View : MvpView {
        fun showLoading(isLoading: Boolean)
        fun showCurrentPinIncorrectError()
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
        fun onSeedPhraseValidated(keys: List<String>)
    }
}

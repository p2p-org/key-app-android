package org.p2p.wallet.auth.ui.pin.create

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import javax.crypto.Cipher

interface CreatePinContract {

    interface View : MvpView {
        fun onAuthFinished()
        fun onPinCreated()
        fun showCreation()
        fun showConfirmation()
        fun showConfirmationError()
        fun lockPinKeyboard()
        fun vibrate(duration: Long)
        fun showLoading(isLoading: Boolean)
        fun showBiometricDialog(cipher: Cipher)
        fun navigateBack()
    }

    interface Presenter : MvpPresenter<View> {
        fun setPinCode(pinCode: String)
        fun enableBiometric()
        fun createPin(cipher: Cipher?)
        fun clearUserData()
    }
}

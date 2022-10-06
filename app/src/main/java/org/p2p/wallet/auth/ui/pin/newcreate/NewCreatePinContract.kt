package org.p2p.wallet.auth.ui.pin.newcreate

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import javax.crypto.Cipher

interface NewCreatePinContract {

    interface View : MvpView {
        fun showCreation()
        fun showConfirmation()
        fun showConfirmationError()
        fun lockPinKeyboard()
        fun vibrate(duration: Long)
        fun showLoading(isLoading: Boolean)
        fun navigateBack()
        fun navigateToMain()
        fun showBiometricDialog(biometricCipher: Cipher)
    }

    interface Presenter : MvpPresenter<View> {
        fun setPinMode(pinMode: PinMode)
        fun setPinCode(pinCode: String)
        fun onBackPressed()
        fun createPin(biometricCipher: Cipher?)
    }
}

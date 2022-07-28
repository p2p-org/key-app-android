package org.p2p.wallet.auth.ui.pin.newcreate

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewCreatePinContract {

    interface View : MvpView {
        fun onPinCreated(pinCode: String)
        fun showCreation()
        fun showConfirmation()
        fun showConfirmationError()
        fun lockPinKeyboard()
        fun vibrate(duration: Long)
        fun showLoading(isLoading: Boolean)
        fun navigateBack()
    }

    interface Presenter : MvpPresenter<View> {
        fun setPinCode(pinCode: String)
    }
}

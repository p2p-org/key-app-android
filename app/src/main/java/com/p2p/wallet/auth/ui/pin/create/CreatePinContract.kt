package com.p2p.wallet.auth.ui.pin.create

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface CreatePinContract {

    interface View : MvpView {
        fun onAuthFinished()
        fun navigateToBiometric(createdPin: String)
        fun showCreation()
        fun showConfirmation()
        fun showConfirmationError()
        fun lockPinKeyboard()
        fun vibrate(duration: Long)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun setPinCode(pinCode: String)
    }
}
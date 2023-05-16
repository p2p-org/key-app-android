package org.p2p.wallet.settings.ui.resetpin.pin

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ResetPinContract {

    interface View : MvpView {
        fun navigateBackToSettings()
        fun showPinCorrect()
        fun showPinConfirmation()
        fun showPinConfirmed()
        fun showMessage(@StringRes messageRes: Int)

        fun showIncorrectPinError()

        fun vibrate(duration: Long)

        fun navigateToOnboarding()
    }

    interface Presenter : MvpPresenter<View> {
        fun setPinCode(pinCode: String)
        fun logout()
    }
}

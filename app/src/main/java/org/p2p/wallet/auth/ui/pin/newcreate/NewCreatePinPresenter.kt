package org.p2p.wallet.auth.ui.pin.newcreate

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString

private const val VIBRATE_DURATION = 500L

class NewCreatePinPresenter(
    private val authLogoutInteractor: AuthLogoutInteractor,
    private val adminAnalytics: AdminAnalytics
) : BasePresenter<NewCreatePinContract.View>(),
    NewCreatePinContract.Presenter {

    private var createdPin = emptyString()

    override fun setPinCode(pinCode: String) {
        if (createdPin.isEmpty()) {
            createdPin = pinCode
            view?.showConfirmation()
            return
        }

        if (pinCode != createdPin) {
            view?.showConfirmationError()
            view?.vibrate(VIBRATE_DURATION)
            adminAnalytics.logPinRejected(ScreenNames.OnBoarding.PIN_CONFIRM)
            return
        }

        view?.lockPinKeyboard()

        view?.onPinCreated(createdPin)
    }

    override fun clearUserData() {
        launch {
            authLogoutInteractor.onUserLogout()
            view?.navigateBack()
        }
    }
}

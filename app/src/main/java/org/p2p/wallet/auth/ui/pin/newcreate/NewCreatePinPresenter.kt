package org.p2p.wallet.auth.ui.pin.newcreate

import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.AuthLogoutInteractor
import org.p2p.wallet.common.analytics.constants.ScreenNames
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.utils.emptyString

private const val VIBRATE_DURATION = 500L

enum class PinMode {
    CREATE,
    CONFIRM
}

class NewCreatePinPresenter(
    private val authLogoutInteractor: AuthLogoutInteractor,
    private val adminAnalytics: AdminAnalytics
) : BasePresenter<NewCreatePinContract.View>(),
    NewCreatePinContract.Presenter {

    private var createdPin = emptyString()

    override var pinMode = PinMode.CREATE

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

        view?.apply {
            lockPinKeyboard()
            onPinCreated(createdPin)
            vibrate(VIBRATE_DURATION)
            createdPin = emptyString()
        }
    }

    override fun onBackPressed() {
        createdPin = emptyString()
        when (pinMode) {
            PinMode.CREATE -> view?.navigateBack()
            PinMode.CONFIRM -> view?.showCreation()
        }
    }
}

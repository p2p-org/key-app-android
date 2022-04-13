package org.p2p.wallet.auth.ui.done

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.push_notifications.ineractor.PushNotificationsInteractor

class AuthDonePresenter(
    private val usernameInteractor: UsernameInteractor,
    private val pushNotificationsInteractor: PushNotificationsInteractor
) : BasePresenter<AuthDoneContract.View>(), AuthDoneContract.Presenter {

    override fun load() {
        val username = usernameInteractor.getUsername()?.username
        view?.showUsername(username)

        // Send device push token to NotificationService on creation and restoring the wallet
        launch {
            pushNotificationsInteractor.updateDeviceToken()
        }
    }
}

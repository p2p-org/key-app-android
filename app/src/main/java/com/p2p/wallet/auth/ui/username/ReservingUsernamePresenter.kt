package com.p2p.wallet.auth.ui.username

import com.p2p.wallet.auth.interactor.ReservingUsernameInteractor
import com.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.launch

class ReservingUsernamePresenter(
    private val interactor: ReservingUsernameInteractor
) :
    BasePresenter<ReservingUsernameContract.View>(),
    ReservingUsernameContract.Presenter {

    override fun checkUsername(username: String) {
        launch {
            interactor.checkUsername(username)
        }
    }

    override fun registerUsername() {
        launch {
            interactor.registerUsername("")
        }
    }
}
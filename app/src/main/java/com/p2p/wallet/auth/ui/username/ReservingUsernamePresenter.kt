package com.p2p.wallet.auth.ui.username

import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.launch

class ReservingUsernamePresenter(
    private val interactor: AuthInteractor
) :
    BasePresenter<ReservingUsernameContract.View>(),
    ReservingUsernameContract.Presenter {

    override fun checkUsername() {
        launch {
        }
    }

    override fun createUsername() {
        launch {
        }
    }
}
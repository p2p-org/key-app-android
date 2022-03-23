package org.p2p.wallet.auth.ui.done

import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter

class AuthDonePresenter(
    private val usernameInteractor: UsernameInteractor
) : BasePresenter<AuthDoneContract.View>(), AuthDoneContract.Presenter {

    override fun load() {
        val username = usernameInteractor.getUsername()?.username
        view?.showUsername(username)
    }
}

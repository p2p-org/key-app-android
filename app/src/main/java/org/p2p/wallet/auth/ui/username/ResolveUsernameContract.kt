package org.p2p.wallet.auth.ui.username

import org.p2p.wallet.auth.model.ResolveUsername
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ResolveUsernameContract {

    interface View : MvpView {
        fun showUsernameResult(names: List<ResolveUsername>)
    }

    interface Presenter : MvpPresenter<View> {
        fun resolveUsername(name: String)
    }
}
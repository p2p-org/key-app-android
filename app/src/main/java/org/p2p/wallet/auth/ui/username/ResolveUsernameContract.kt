package org.p2p.wallet.auth.ui.username

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ResolveUsernameContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
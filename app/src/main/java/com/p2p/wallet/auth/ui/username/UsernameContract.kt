package com.p2p.wallet.auth.ui.username

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface UsernameContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
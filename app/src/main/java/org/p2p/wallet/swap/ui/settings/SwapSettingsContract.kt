package org.p2p.wallet.swap.ui.settings

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.main.model.Token

interface SwapSettingsContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View> {
        fun setFeePayToken(token: Token.Active)
    }
}
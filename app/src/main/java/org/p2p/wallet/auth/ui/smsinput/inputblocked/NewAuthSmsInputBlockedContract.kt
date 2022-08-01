package org.p2p.wallet.auth.ui.smsinput.inputblocked

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewAuthSmsInputBlockedContract {
    interface View : MvpView {
        fun renderTimerBeforeUnblockValue(formattedTime: String)
    }

    interface Presenter : MvpPresenter<View>
}

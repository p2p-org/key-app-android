package org.p2p.wallet.auth.ui.verify

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface VerifySecurityKeyContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
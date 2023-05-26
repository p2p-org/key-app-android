package org.p2p.wallet.striga.ui.personaldata

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface StrigaPersonalInfoContract {
    interface View : MvpView
    interface Presenter : MvpPresenter<View>
}

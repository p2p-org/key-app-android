package org.p2p.wallet.referral

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ReferralContract {
    interface View : MvpView
    interface Presenter : MvpPresenter<View>
}

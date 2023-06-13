package org.p2p.wallet.striga.kyc.ui

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.kyc.sdk.StrigaSdkInitParams

interface StrigaKycContract {
    interface View : MvpView {
        fun startKyc(initParams: StrigaSdkInitParams)
        fun navigateBack()
    }

    interface Presenter : MvpPresenter<View>
}

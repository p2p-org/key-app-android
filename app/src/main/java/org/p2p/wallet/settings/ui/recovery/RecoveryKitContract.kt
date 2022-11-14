package org.p2p.wallet.settings.ui.recovery

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface RecoveryKitContract {
    interface View : MvpView {
        fun showDeviceData(device: String)
        fun showPhoneData(phone: String)
        fun showSocialData(social: String)
    }

    interface Presenter : MvpPresenter<View>
}

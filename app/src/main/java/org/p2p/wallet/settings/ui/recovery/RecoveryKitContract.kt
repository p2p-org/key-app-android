package org.p2p.wallet.settings.ui.recovery

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface RecoveryKitContract {
    interface View : MvpView {
        fun showDeviceName(deviceName: String)
        fun showPhoneNumber(phoneNumber: String)
        fun showSocialId(socialId: String)
    }

    interface Presenter : MvpPresenter<View>
}

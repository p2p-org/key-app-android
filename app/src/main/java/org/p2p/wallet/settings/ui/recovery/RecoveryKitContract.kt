package org.p2p.wallet.settings.ui.recovery

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface RecoveryKitContract {
    interface View : MvpView {
        fun showDeviceName(deviceName: CharSequence, isDifferentFromDeviceShare: Boolean)
        fun showManageDevice(isVisible: Boolean)
        fun showPhoneNumber(phoneNumber: String)
        fun showSocialId(socialId: String)
        fun setWebAuthInfoVisibility(isVisible: Boolean)
        fun showLogoutInfoDialog()
        fun showSeedPhraseLockFragment()
    }

    interface Presenter : MvpPresenter<View> {
        fun onSeedPhraseClicked()
        fun logout()
    }
}

package org.p2p.wallet.settings.ui.security

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SecurityAndPrivacyContract {
    interface View : MvpView {
        fun showDeviceName(deviceName: CharSequence, showWarning: Boolean)
        fun showManageVisible(isVisible: Boolean)
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

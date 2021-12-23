package org.p2p.wallet.auth.ui.security

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SecurityKeyContract {

    interface View : MvpView {
        fun showKeys(keys: List<String>)
        fun copyToClipboard(keys: List<String>)
        fun showLoading(isLoading: Boolean)
        fun navigateToReserve()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadKeys()
        fun copyKeys()
        fun createAndSaveAccount()
    }
}
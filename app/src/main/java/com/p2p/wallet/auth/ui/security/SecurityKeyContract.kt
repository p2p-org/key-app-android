package com.p2p.wallet.auth.ui.security

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface SecurityKeyContract {

    interface View : MvpView {
        fun showKeys(keys: List<String>)
        fun copyToClipboard(keys: List<String>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadKeys()
        fun copyKeys()
    }
}
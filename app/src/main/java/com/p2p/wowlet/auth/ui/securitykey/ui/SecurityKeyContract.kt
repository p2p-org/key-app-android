package com.p2p.wowlet.auth.ui.securitykey.ui

import com.p2p.wowlet.common.mvp.MvpPresenter
import com.p2p.wowlet.common.mvp.MvpView

interface SecurityKeyContract {

    interface View : MvpView {
        fun showPhrases(phrases: List<String>)
        fun copyToClipboard(phrases: List<String>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadPhrases()
        fun copyPhrases()
    }
}
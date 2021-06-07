package com.p2p.wallet.settings.ui.appearance

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.settings.interactor.Theme

interface AppearanceContract {

    interface View : MvpView {
        fun showCurrentTheme(themeButtonId: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadThemeSettings()
        fun setTheme(theme: Theme)
    }
}
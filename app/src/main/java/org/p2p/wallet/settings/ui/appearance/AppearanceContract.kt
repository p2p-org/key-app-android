package org.p2p.wallet.settings.ui.appearance

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.settings.interactor.Theme

interface AppearanceContract {

    interface View : MvpView {
        fun showCurrentTheme(themeButtonId: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadThemeSettings()
        fun setTheme(theme: Theme)
    }
}

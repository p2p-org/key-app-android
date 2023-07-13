package org.p2p.wallet.home.ui.container

import androidx.annotation.MenuRes
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.deeplinks.DeeplinkData

interface MainContainerContract {

    interface View : MvpView {
        fun setMainNavigationConfiguration(screensConfiguration: List<ScreenConfiguration>)
        fun inflateBottomNavigationMenu(@MenuRes menuRes: Int)
        fun showSettingsBadgeVisible(isVisible: Boolean)
        fun showCryptoBadgeVisible(isVisible: Boolean)
        fun navigateFromDeeplink(data: DeeplinkData)

        fun navigateToSendNoTokens(fallbackToken: Token)
        fun navigateToSendScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadMainNavigation()
        fun loadBottomNavigationMenu()
        fun initializeDeeplinks()
        fun observeUserTokens()
        fun logHomeOpened()
        fun logHistoryOpened()
        fun logSettingsOpened()
        fun onSendClicked()
    }
}

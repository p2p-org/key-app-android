package org.p2p.wallet.home.ui.container

import androidx.annotation.MenuRes
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.deeplinks.DeeplinkData
import org.p2p.wallet.home.deeplinks.DeeplinkScreenNavigator

interface MainContainerContract {

    interface View : MvpView, DeeplinkScreenNavigator {
        fun setMainNavigationConfiguration(screensConfiguration: List<ScreenConfiguration>)
        fun inflateBottomNavigationMenu(@MenuRes menuRes: Int)
        fun showSettingsBadgeVisible(isVisible: Boolean)
        fun showCryptoBadgeVisible(isVisible: Boolean)
        fun showWalletBadgeVisible(isVisible: Boolean)
        fun showWalletBalance(balance: String)
        fun navigateToTabFromDeeplink(data: DeeplinkData)

        fun navigateToSendNoTokens(fallbackToken: Token)
        fun navigateToSendScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadMainNavigation()
        fun loadBottomNavigationMenu()
        fun initializeDeeplinks()
        fun observeUserTokens()
        fun logWalletOpened()
        fun logCryptoOpened()
        fun logHistoryOpened()
        fun logSettingsOpened()
        fun onSendClicked()
    }
}

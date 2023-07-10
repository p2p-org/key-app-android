package org.p2p.wallet.home.ui.container

import androidx.annotation.MenuRes
import kotlinx.coroutines.CoroutineScope
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.deeplinks.DeeplinkData

interface MainContainerContract {

    interface View : MvpView {
        fun setMainNavigationConfiguration(screensConfiguration: List<ScreenConfiguration>)
        fun inflateBottomNavigationMenu(@MenuRes menuRes: Int)
        fun showConnectionError(isVisible: Boolean)
        fun showSettingsBadgeVisible(isVisible: Boolean)
        fun navigateFromDeeplink(data: DeeplinkData)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadMainNavigation()
        fun loadBottomNavigationMenu()
        fun launchInternetObserver(coroutineScope: CoroutineScope)
        fun initializeDeeplinks()
        fun logHomeOpened()
        fun logHistoryOpened()
        fun logSettingsOpened()
    }
}

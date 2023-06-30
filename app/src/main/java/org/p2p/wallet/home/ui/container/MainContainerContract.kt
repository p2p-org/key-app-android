package org.p2p.wallet.home.ui.container

import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.deeplinks.DeeplinkData

interface MainContainerContract {

    interface View : MvpView {
        fun setMainNavigationConfiguration(
            screensConfigurationMap: Map<ScreenTab, KClass<out Fragment>>,
            screensConfigurationArguments: List<Bundle?>
        )

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
        fun logSwapOpened()
        fun logHomeOpened()
        fun logEarnOpened()
        fun logHistoryOpened()
        fun logSettingsOpened()
    }
}

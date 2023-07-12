package org.p2p.wallet.home.ui.container

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.p2p.core.network.ConnectionManager
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.ui.main.HomeFragment
import org.p2p.wallet.home.ui.wallet.WalletFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment

class MainContainerPresenter(
    private val deeplinksManager: AppDeeplinksManager,
    private val connectionManager: ConnectionManager,
    private val metadataInteractor: MetadataInteractor,
    private val homeAnalytics: HomeAnalytics
) : BasePresenter<MainContainerContract.View>(), MainContainerContract.Presenter {

    override fun attach(view: MainContainerContract.View) {
        super.attach(view)
        observeInternetState()
    }

    override fun loadMainNavigation() {
        view?.setMainNavigationConfiguration(getScreenConfiguration())
    }

    private fun getScreenConfiguration(): List<ScreenConfiguration> = buildList {
        add(ScreenConfiguration(ScreenTab.WALLET_SCREEN, WalletFragment::class))
        add(ScreenConfiguration(ScreenTab.MY_CRYPTO_SCREEN, HomeFragment::class))
        add(ScreenConfiguration(ScreenTab.HISTORY_SCREEN, HistoryFragment::class))
        add(ScreenConfiguration(ScreenTab.SETTINGS_SCREEN, SettingsFragment::class))
    }

    override fun loadBottomNavigationMenu() {
        view?.inflateBottomNavigationMenu(menuRes = R.menu.menu_ui_kit_bottom_navigation)

        checkDeviceShare()
    }

    override fun initializeDeeplinks() {
        val supportedTargets = setOf(
            DeeplinkTarget.MY_CRYPTO,
            DeeplinkTarget.HISTORY,
            DeeplinkTarget.SETTINGS,
        )
        deeplinksManager.subscribeOnDeeplinks(supportedTargets)
            .onEach { view?.navigateFromDeeplink(it) }
            .launchIn(this)

        deeplinksManager.executeHomePendingDeeplink()
        deeplinksManager.executeTransferPendingAppLink()
    }

    override fun logHomeOpened() {
        homeAnalytics.logBottomNavigationHomeClicked()
    }

    override fun logHistoryOpened() {
        homeAnalytics.logBottomNavigationHistoryClicked()
    }

    override fun logSettingsOpened() {
        homeAnalytics.logBottomNavigationSettingsClicked()
    }

    private fun observeInternetState() {
        connectionManager.connectionStatus
            .onEach { isConnected ->
                if (!isConnected) view?.showUiKitSnackBar(messageResId = R.string.error_no_internet_message)
            }
            .launchIn(this)
    }

    private fun checkDeviceShare() {
        val hasDifferentDeviceShare = metadataInteractor.hasDifferentDeviceShare()
        view?.showSettingsBadgeVisible(isVisible = hasDifferentDeviceShare)
    }
}

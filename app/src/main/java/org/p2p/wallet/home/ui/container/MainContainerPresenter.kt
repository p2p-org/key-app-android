package org.p2p.wallet.home.ui.container

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SolendEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.interactor.RefreshErrorInteractor
import org.p2p.wallet.home.ui.main.HomeFragment
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.solend.ui.earn.StubSolendEarnFragment
import org.p2p.wallet.swap.analytics.SwapAnalytics

class MainContainerPresenter(
    private val refreshErrorInteractor: RefreshErrorInteractor,
    private val deeplinksManager: AppDeeplinksManager,
    private val connectionManager: ConnectionManager,
    private val metadataInteractor: MetadataInteractor,
    private val sellInteractor: SellInteractor,
    private val swapAnalytics: SwapAnalytics,
    private val solendFeatureToggle: SolendEnabledFeatureToggle,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val homeAnalytics: HomeAnalytics
) : BasePresenter<MainContainerContract.View>(), MainContainerContract.Presenter {

    override fun attach(view: MainContainerContract.View) {
        super.attach(view)
        observeRefreshEvent()
    }

    override fun loadMainNavigation() {
        val screensConfiguration = getScreenConfiguration()
        val screensConfigurationMap = screensConfiguration.associate { it.screen to it.kClass }
        val screensConfigurationArguments = screensConfiguration.map { it.bundle }
        view?.setMainNavigationConfiguration(screensConfigurationMap, screensConfigurationArguments)
    }

    private fun getScreenConfiguration() = buildList {
        add(ScreenConfiguration(ScreenTab.HOME_SCREEN, HomeFragment::class))
        when {
            sellEnabledFeatureToggle.isFeatureEnabled -> add(
                ScreenConfiguration(
                    ScreenTab.SWAP_SCREEN,
                    JupiterSwapFragment::class,
                    JupiterSwapFragment.createBundle(source = SwapOpenedFrom.BOTTOM_NAVIGATION)
                )
            )
            solendFeatureToggle.isFeatureEnabled -> add(
                ScreenConfiguration(
                    ScreenTab.EARN_SCREEN,
                    StubSolendEarnFragment::class
                )
            )
        }
        add(ScreenConfiguration(ScreenTab.HISTORY_SCREEN, HistoryFragment::class))
        add(ScreenConfiguration(ScreenTab.SETTINGS_SCREEN, SettingsFragment::class))
    }

    override fun loadBottomNavigationMenu() {
        val menuRes = when {
            solendFeatureToggle.isFeatureEnabled -> R.menu.menu_ui_kit_bottom_navigation_earn
            sellEnabledFeatureToggle.isFeatureEnabled -> R.menu.menu_ui_kit_bottom_navigation_sell
            else -> R.menu.menu_ui_kit_bottom_navigation
        }

        view?.inflateBottomNavigationMenu(menuRes = menuRes)

        checkDeviceShare()
    }

    override fun launchInternetObserver(coroutineScope: CoroutineScope) {
        connectionManager.connectionStatus
            .onEach { isConnected ->
                if (!isConnected) view?.showConnectionError(isVisible = true)
            }
            .launchIn(coroutineScope)
    }

    override fun initializeDeeplinks() {
        val supportedTargets = setOf(
            DeeplinkTarget.HOME,
            DeeplinkTarget.HISTORY,
            DeeplinkTarget.SETTINGS,
        )
        deeplinksManager.subscribeOnDeeplinks(supportedTargets)
            .onEach { view?.navigateFromDeeplink(it) }
            .launchIn(this)

        deeplinksManager.executeHomePendingDeeplink()
        deeplinksManager.executeTransferPendingAppLink()
    }

    override fun logSwapOpened() {
        launch {
            val isSellEnabled = sellInteractor.isSellAvailable()
            swapAnalytics.logSwapOpenedFromMain(isSellEnabled = isSellEnabled)
        }
    }

    override fun logHomeOpened() {
        homeAnalytics.logBottomNavigationHomeClicked()
    }

    override fun logEarnOpened() {
        homeAnalytics.logBottomNavigationEarnClicked()
    }

    override fun logHistoryOpened() {
        homeAnalytics.logBottomNavigationHistoryClicked()
    }

    override fun logSettingsOpened() {
        homeAnalytics.logBottomNavigationSettingsClicked()
    }

    private fun observeRefreshEvent() {
        refreshErrorInteractor.getRefreshEventFlow()
            .onEach {
                if (connectionManager.connectionStatus.value) {
                    view?.showConnectionError(isVisible = false)
                }
            }
            .launchIn(this)
    }

    private fun checkDeviceShare() {
        val hasDifferentDeviceShare = metadataInteractor.hasDifferentDeviceShare()
        view?.showSettingsBadgeVisible(isVisible = hasDifferentDeviceShare)
    }
}

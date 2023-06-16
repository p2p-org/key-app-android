package org.p2p.wallet.home.ui.container

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SolendEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.home.interactor.RefreshErrorInteractor
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.swap.analytics.SwapAnalytics

class MainContainerPresenter(
    private val refreshErrorInteractor: RefreshErrorInteractor,
    private val deeplinksManager: AppDeeplinksManager,
    private val connectionManager: ConnectionManager,
    private val metadataInteractor: MetadataInteractor,
    private val sellInteractor: SellInteractor,
    private val swapAnalytics: SwapAnalytics,
    private val solendFeatureToggle: SolendEnabledFeatureToggle,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle
) : BasePresenter<MainContainerContract.View>(), MainContainerContract.Presenter {

    override fun attach(view: MainContainerContract.View) {
        super.attach(view)
        observeRefreshEvent()
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

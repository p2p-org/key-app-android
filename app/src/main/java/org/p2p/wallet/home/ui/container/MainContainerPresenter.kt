package org.p2p.wallet.home.ui.container

import timber.log.Timber
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.deeplinks.DeeplinkHandler
import org.p2p.wallet.home.ui.crypto.MyCryptoFragment
import org.p2p.wallet.home.ui.wallet.WalletFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.unsafeLazy

class MainContainerPresenter(
    private val deeplinksManager: AppDeeplinksManager,
    private val connectionManager: ConnectionManager,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val metadataInteractor: MetadataInteractor,
    private val userInteractor: UserInteractor,
    private val homeAnalytics: HomeAnalytics,
    private val newBuyFeatureToggle: NewBuyFeatureToggle,
) : BasePresenter<MainContainerContract.View>(), MainContainerContract.Presenter {

    private val deeplinkHandler by unsafeLazy {
        DeeplinkHandler(
            coroutineScope = this,
            screenNavigator = view,
            tokenServiceCoordinator = tokenServiceCoordinator,
            userInteractor = userInteractor,
            newBuyFeatureToggle = newBuyFeatureToggle,
            deeplinkTopLevelHandler = ::handleDeeplinkTarget
        )
    }

    override fun attach(view: MainContainerContract.View) {
        super.attach(view)
        handleDeeplinks()
        observeInternetState()
    }

    override fun loadMainNavigation() {
        view?.setMainNavigationConfiguration(getScreenConfiguration())
    }

    private fun getScreenConfiguration(): List<ScreenConfiguration> = buildList {
        add(ScreenConfiguration(ScreenTab.WALLET_SCREEN, WalletFragment::class))
        // add(ScreenConfiguration(ScreenTab.MY_CRYPTO_SCREEN, HomeFragment::class))
        add(ScreenConfiguration(ScreenTab.MY_CRYPTO_SCREEN, MyCryptoFragment::class))
        // TODO PWN-9151 migrate on crypto fragment after striga move to Wallet Screen
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
        deeplinksManager.apply {
            subscribeOnDeeplinks(supportedTargets)
                .onEach { view?.navigateFromDeeplink(it) }
                .launchIn(this@MainContainerPresenter)

            executeHomePendingDeeplink()
            executeTransferPendingAppLink()
        }
    }

    override fun observeUserTokens() {
        launch {
            tokenServiceCoordinator.observeUserTokens()
                .collect { handleTokenState(it) }
        }
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

    override fun onSendClicked() {
        launch {
            val userTokens = tokenServiceCoordinator.getUserTokens()
            if (userTokens.isEmpty()) {
                val validTokenToBuy = userInteractor.getSingleTokenForBuy() ?: return@launch
                view?.navigateToSendNoTokens(validTokenToBuy)
            } else {
                view?.navigateToSendScreen()
            }
        }
    }

    private fun observeInternetState() {
        connectionManager.connectionStatus
            .onEach { isConnected ->
                if (!isConnected) view?.showUiKitSnackBar(messageResId = R.string.error_no_internet_message)
            }
            .launchIn(this)
    }

    private fun handleDeeplinks() {
        launchSupervisor {
            deeplinksManager.subscribeOnDeeplinks(
                setOf(
                    DeeplinkTarget.BUY,
                    DeeplinkTarget.SEND,
                    DeeplinkTarget.SWAP,
                    DeeplinkTarget.CASH_OUT
                )
            ).collect(deeplinkHandler::handle)
        }
    }

    private fun handleDeeplinkTarget(target: DeeplinkTarget) {
        when (target) {
            DeeplinkTarget.SEND -> onSendClicked()
            else -> Timber.d("Unsupported deeplink target! $target")
        }
    }

    private fun checkDeviceShare() {
        val hasDifferentDeviceShare = metadataInteractor.hasDifferentDeviceShare()
        view?.showSettingsBadgeVisible(isVisible = hasDifferentDeviceShare)
    }

    private fun handleTokenState(newState: UserTokensState) {
        when (newState) {
            is UserTokensState.Idle -> Unit
            is UserTokensState.Loading -> Unit
            is UserTokensState.Refreshing -> Unit
            is UserTokensState.Error -> Unit
            is UserTokensState.Empty -> {
                view?.showCryptoBadgeVisible(isVisible = false)
            }
            is UserTokensState.Loaded -> {
                view?.showCryptoBadgeVisible(isVisible = newState.ethTokens.isNotEmpty())
            }
        }
    }
}

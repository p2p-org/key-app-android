package org.p2p.wallet.home.ui.container

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.core.token.filterTokensForWalletScreen
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.home.deeplinks.DeeplinkHandler
import org.p2p.wallet.home.ui.container.mapper.WalletBalanceMapper
import org.p2p.wallet.home.ui.crypto.MyCryptoFragment
import org.p2p.wallet.home.ui.wallet.analytics.MainScreenAnalytics
import org.p2p.wallet.home.ui.wallet.interactor.WalletStrigaInteractor
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
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
    private val walletStrigaInteractor: WalletStrigaInteractor,
    private val balanceMapper: WalletBalanceMapper,
    private val mainScreenAnalytics: MainScreenAnalytics
) : BasePresenter<MainContainerContract.View>(), MainContainerContract.Presenter {

    private val deeplinkHandler by unsafeLazy {
        DeeplinkHandler(
            coroutineScope = this,
            screenNavigator = view,
            tokenServiceCoordinator = tokenServiceCoordinator,
            userInteractor = userInteractor,
            deeplinkTopLevelHandler = ::handleDeeplinkTarget
        )
    }

    override fun attach(view: MainContainerContract.View) {
        super.attach(view)
        observeInternetState()
    }

    override fun loadMainNavigation() {
        view?.setMainNavigationConfiguration(getScreenConfiguration())
    }

    private fun getScreenConfiguration(): List<ScreenConfiguration> = buildList {
        add(ScreenConfiguration(ScreenTab.WALLET_SCREEN, MyCryptoFragment::class))
        add(
            ScreenConfiguration(
                screen = ScreenTab.SWAP_SCREEN,
                kClass = JupiterSwapFragment::class,
                bundle = JupiterSwapFragment.createArgs(source = SwapOpenedFrom.BOTTOM_NAVIGATION)
            )
        )
        add(ScreenConfiguration(ScreenTab.HISTORY_SCREEN, HistoryFragment::class))
        add(ScreenConfiguration(ScreenTab.SETTINGS_SCREEN, SettingsFragment::class))
    }

    override fun loadBottomNavigationMenu() {
        view?.inflateBottomNavigationMenu(menuRes = R.menu.menu_ui_kit_bottom_navigation)

        checkIncomeTransfers()
        checkDeviceShare()
    }

    override fun initializeDeeplinks() {
        val supportedTargets = setOf(
            DeeplinkTarget.HISTORY,
            DeeplinkTarget.SETTINGS,
            DeeplinkTarget.BUY,
            DeeplinkTarget.SEND,
            DeeplinkTarget.SWAP,
            DeeplinkTarget.CASH_OUT
        )
        launchSupervisor {
            deeplinksManager.subscribeOnDeeplinks(supportedTargets)
                .onEach { view?.navigateFromDeeplink(it) }
                .collect(deeplinkHandler::handle)
            deeplinksManager.executeHomePendingDeeplink()
            deeplinksManager.executeTransferPendingAppLink()
        }
    }

    override fun observeUserTokens() {
        launch {
            tokenServiceCoordinator.observeUserTokens()
                .collect { handleTokenState(it) }
        }
    }

    override fun logWalletOpened() {
        mainScreenAnalytics.logMainScreenMainClick()
    }

    override fun logCryptoOpened() {
        mainScreenAnalytics.logMainScreenCryptoClick()
    }

    override fun logHistoryOpened() {
        mainScreenAnalytics.logMainScreenHistoryClick()
    }

    override fun logSettingsOpened() {
        mainScreenAnalytics.logMainScreenSettingsClick()
    }

    override fun onSendClicked() {
        mainScreenAnalytics.logMainScreenSendClick()
        launch {
            val userTokens = tokenServiceCoordinator.getUserTokens()
            val isAccountEmpty = userTokens.isEmpty() || userTokens.all { it.isZero }
            if (isAccountEmpty) {
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

    private fun checkIncomeTransfers() {
        launch {
            walletStrigaInteractor.observeOnOffRampTokens()
                .map { it.hasTokens }
                .collect {
                    view?.showWalletBadgeVisible(isVisible = it)
                }
        }
    }

    private fun handleTokenState(newState: UserTokensState) {
        when (newState) {
            is UserTokensState.Idle -> Unit
            is UserTokensState.Loading -> Unit
            is UserTokensState.Refreshing -> Unit
            is UserTokensState.Error -> {
                view?.showWalletBalance(balanceMapper.formatBalance(BigDecimal.ZERO))
            }
            is UserTokensState.Empty -> {
                view?.showCryptoBadgeVisible(isVisible = false)
                view?.showWalletBalance(balanceMapper.formatBalance(BigDecimal.ZERO))
            }
            is UserTokensState.Loaded -> {
                // todo: this new filter supposed to be used for new design
                val filteredTokens = newState.solTokens.filterTokensForWalletScreen()
                val balance = filteredTokens.sumOf { it.total }
                view?.showWalletBalance(balanceMapper.formatBalance(balance))
                view?.showWalletBalance("Wallet")
                view?.showCryptoBadgeVisible(isVisible = newState.ethTokens.isNotEmpty())
            }
        }
    }
}

package org.p2p.wallet.home.ui.container

import java.math.BigDecimal
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.core.token.Token
import org.p2p.core.token.filterTokensForWalletScreen
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.deeplinks.DeeplinkTarget
import org.p2p.wallet.history.ui.history.HistoryFragment
import org.p2p.wallet.home.ui.container.mapper.WalletBalanceMapper
import org.p2p.wallet.home.ui.crypto.MyCryptoFragment
import org.p2p.wallet.home.ui.wallet.analytics.MainScreenAnalytics
import org.p2p.wallet.home.ui.wallet.interactor.WalletStrigaInteractor
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.jupiter.ui.main.JupiterSwapFragment
import org.p2p.wallet.settings.ui.settings.SettingsFragment
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState
import org.p2p.wallet.updates.SocketUpdatesManager

class MainContainerPresenter(
    private val deeplinksManager: AppDeeplinksManager,
    private val connectionManager: ConnectionManager,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val metadataInteractor: MetadataInteractor,
    private val walletStrigaInteractor: WalletStrigaInteractor,
    private val socketUpdatesManager: SocketUpdatesManager,
    private val balanceMapper: WalletBalanceMapper,
    private val mainScreenAnalytics: MainScreenAnalytics,
) : BasePresenter<MainContainerContract.View>(), MainContainerContract.Presenter {

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
        // deeplinks that should be handled only at MainContainer level, because it's a tab switching
        val supportedTargets = setOf(
            DeeplinkTarget.HISTORY,
            DeeplinkTarget.SETTINGS
        )
        launchSupervisor {
            deeplinksManager.subscribeOnDeeplinks(supportedTargets)
                .onEach { view?.navigateToTabFromDeeplink(it) }
                .launchIn(this)
        }
    }

    override fun observeUserTokens() {
        tokenServiceCoordinator.observeUserTokens()
            .onEach(::handleTokenState)
            .launchIn(this)
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

    private fun observeInternetState() {
        connectionManager.connectionStatus
            .onEach { isConnected ->
                if (!isConnected) {
                    view?.showUiKitSnackBar(messageResId = R.string.error_no_internet_message)
                    socketUpdatesManager.stop()
                } else {
                    if (!socketUpdatesManager.isStarted()) {
                        socketUpdatesManager.restart()
                    }
                }
            }
            .launchIn(this)
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
                val balance = filteredTokens.sumOf(Token.Active::total)
                view?.showWalletBalance(balance = balanceMapper.formatBalance(balance))
                view?.showWalletBalance(balance = "Wallet")
                view?.showCryptoBadgeVisible(isVisible = newState.ethTokens.isNotEmpty())
            }
        }
    }
}

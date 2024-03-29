package org.p2p.wallet.home.ui.crypto

import android.content.SharedPreferences
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.core.token.filterTokensForWalletScreen
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.scaleTwo
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.interactor.MyCryptoInteractor
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.crypto.analytics.CryptoScreenAnalytics
import org.p2p.wallet.home.ui.crypto.handlers.BridgeClaimBundleClickHandler
import org.p2p.wallet.home.ui.crypto.mapper.MyCryptoMapper
import org.p2p.wallet.home.ui.wallet.analytics.MainScreenAnalytics
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.pnl.interactor.PnlDataObserver
import org.p2p.wallet.pnl.interactor.PnlDataState
import org.p2p.wallet.pnl.ui.PnlUiMapper
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState
import org.p2p.wallet.utils.ellipsizeAddress
import org.p2p.wallet.utils.unsafeLazy

private val MINIMAL_DUST_FOR_BALANCE = BigDecimal(0.01)

class MyCryptoPresenter(
    private val cryptoInteractor: MyCryptoInteractor,
    private val cryptoMapper: MyCryptoMapper,
    private val connectionManager: ConnectionManager,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val cryptoScreenAnalytics: CryptoScreenAnalytics,
    private val claimHandler: BridgeClaimBundleClickHandler,
    private val sharedPreferences: SharedPreferences,
    private val usernameInteractor: UsernameInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val mainScreenAnalytics: MainScreenAnalytics,
    private val pnlDataObserver: PnlDataObserver,
    private val pnlUiMapper: PnlUiMapper,
) : BasePresenter<MyCryptoContract.View>(), MyCryptoContract.Presenter {

    private var currentVisibilityState: VisibilityState = if (cryptoInteractor.getHiddenTokensVisibility()) {
        VisibilityState.Visible
    } else {
        VisibilityState.Hidden
    }

    private val sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == SettingsInteractor.KEY_HIDDEN_ZERO_BALANCE) {
            observeCryptoTokens()
        }
    }
    private val userPublicKey: String by unsafeLazy { tokenKeyProvider.publicKey }
    private var username: Username? = null

    private var cryptoTokensSubscription: Job? = null
    private var pnlDataSubscription: Job? = null

    override fun attach(view: MyCryptoContract.View) {
        super.attach(view)
        showUserAddressAndUsername()
        prepareAndShowActionButtons()
        observeCryptoTokens()
        observePnlData()
        cryptoScreenAnalytics.logCryptoScreenOpened()
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    override fun refreshTokens() {
        launchInternetAware(connectionManager) {
            try {
                tokenServiceCoordinator.refresh()
                // pnl must restart it's 5 minutes timer after force refresh
                // and we should restart it only if it was initially started
                // other cases might indicate that we didn't start observer due to empty state
                if (pnlDataObserver.isStarted()) {
                    pnlDataObserver.restartAndRefresh()
                }
            } catch (cancelled: CancellationException) {
                Timber.i("Loading tokens job cancelled")
            } catch (error: Throwable) {
                Timber.e(error, "Error refreshing user tokens")
                view?.showErrorMessage(error)
            }
        }
    }

    override fun onTokenClicked(token: Token.Active) {
        cryptoScreenAnalytics.logCryptoTokenClick(tokenName = token.tokenName, tokenSymbol = token.tokenSymbol)
        view?.showTokenHistory(token)
    }

    override fun onAmountClicked() {
        cryptoScreenAnalytics.logCryptoAmountClick()
    }

    private fun observeCryptoTokens() {
        cryptoTokensSubscription?.cancel()
        cryptoTokensSubscription = tokenServiceCoordinator.observeUserTokens()
            .onEach { handleTokenState(it) }
            .launchIn(this)
    }

    private fun observePnlData() {
        pnlDataSubscription?.cancel()
        pnlDataSubscription = pnlDataObserver.pnlState
            .onEach { handleTokenState(tokenServiceCoordinator.observeLastState().value) }
            .launchIn(this)
    }

    private fun handleTokenState(newState: UserTokensState) {
        view?.showRefreshing(isRefreshing = newState.isLoading())

        when (newState) {
            is UserTokensState.Idle -> Unit
            is UserTokensState.Loading -> Unit
            is UserTokensState.Refreshing -> Unit
            is UserTokensState.Error -> Unit
            is UserTokensState.Empty -> {
                view?.showEmptyState(isEmpty = true)
                handleEmptyAccount()
            }
            is UserTokensState.Loaded -> {
                view?.showEmptyState(isEmpty = false)

                if (!pnlDataObserver.isStarted()) {
                    pnlDataObserver.start()
                }

                showTokensAndBalance(
                    pnlDataState = pnlDataObserver.pnlState.value,
                    // separated screens logic: solTokens = filterCryptoTokens(newState.solTokens),
                    solTokens = newState.solTokens,
                    ethTokens = newState.ethTokens
                )
            }
        }
    }

    private fun filterCryptoTokens(solTokens: List<Token.Active>): List<Token.Active> {
        val excludedTokens = solTokens.filterTokensForWalletScreen()
        return solTokens.minus(excludedTokens.toSet())
    }

    private fun showTokensAndBalance(
        pnlDataState: PnlDataState,
        solTokens: List<Token.Active>,
        ethTokens: List<Token.Eth>
    ) {
        val balance = getUserBalance(solTokens)
        view?.showBalance(cryptoMapper.mapBalance(balance))
        view?.showBalancePnl(pnlUiMapper.mapBalancePnl(pnlDataState))
        cryptoScreenAnalytics.logUserAggregateBalanceBase(balance)

        val areZerosHidden = cryptoInteractor.areZerosHidden()
        if (ethTokens.isNotEmpty()) {
            cryptoScreenAnalytics.logCryptoClaimTransferedViewed(ethTokens.size)
        }
        val mappedItems: List<AnyCellItem> = cryptoMapper.mapToCellItems(
            pnlDataState = pnlDataState,
            tokens = solTokens,
            ethereumTokens = ethTokens,
            visibilityState = currentVisibilityState,
            isZerosHidden = areZerosHidden,
        )
        view?.showItems(mappedItems)
    }

    override fun onBalancePnlClicked() {
        if (pnlDataObserver.pnlState.value.isLoaded()) {
            view?.showPnlDetails(pnlDataObserver.pnlState.value.toLoadedOrNull()!!.total.percent)
        }
    }

    private fun showUserAddressAndUsername() {
        this.username = usernameInteractor.getUsername()
        val userAddress = username?.fullUsername ?: userPublicKey.ellipsizeAddress(6)
        view?.showUserAddress(userAddress)
    }

    private fun getUserBalance(tokens: List<Token.Active>): BigDecimal {
        if (tokens.none { it.totalInUsd != null }) return BigDecimal.ZERO

        return tokens
            .mapNotNull(Token.Active::totalInUsd)
            .filter { it.isMoreThan(MINIMAL_DUST_FOR_BALANCE) }
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleTwo()
    }

    private fun handleEmptyAccount() {
        cryptoScreenAnalytics.logUserAggregateBalanceBase(BigDecimal.ZERO)
        view?.showBalancePnl(null)
        view?.showBalance(cryptoMapper.mapBalance(BigDecimal.ZERO))
        view?.showItems(cryptoMapper.mapEmptyStateCellItems())
    }

    override fun toggleTokenVisibility(token: Token.Active) {
        launch {
            val handleDefaultVisibility = { token: Token.Active ->
                if (cryptoInteractor.areZerosHidden() && token.isZero) {
                    TokenVisibility.SHOWN
                } else {
                    TokenVisibility.HIDDEN
                }
            }
            val newVisibility = when (token.visibility) {
                TokenVisibility.SHOWN -> TokenVisibility.HIDDEN
                TokenVisibility.HIDDEN -> TokenVisibility.SHOWN
                TokenVisibility.DEFAULT -> handleDefaultVisibility(token)
            }

            cryptoInteractor.setTokenHidden(
                mintAddress = token.mintAddress,
                visibility = newVisibility.stringValue
            )
        }
    }

    override fun toggleTokenVisibilityState() {
        currentVisibilityState = currentVisibilityState.toggle()
        cryptoInteractor.setHiddenTokensVisibility(currentVisibilityState.isVisible)
        observeCryptoTokens()
    }

    private fun prepareAndShowActionButtons() {
        val buttons = listOf(
            ActionButton.SELL_BUTTON,
            ActionButton.BUY_BUTTON,
            ActionButton.SWAP_BUTTON,
            ActionButton.RECEIVE_BUTTON,
            ActionButton.SEND_BUTTON
        )
        view?.showActionButtons(buttons)
    }

    override fun onBuyClicked() {
        mainScreenAnalytics.logMainScreenAddMoneyClick()
        view?.showAddMoneyDialog()
    }

    override fun onReceiveClicked() {
        cryptoScreenAnalytics.logCryptoReceiveClick()
        view?.navigateToReceive()
    }

    override fun onSwapClicked() {
        cryptoScreenAnalytics.logCryptoSwapClick()
        view?.navigateToSwap()
    }

    override fun onSellClicked() {
        view?.navigateToSell()
    }

    override fun onClaimClicked(canBeClaimed: Boolean, token: Token.Eth) {
        cryptoScreenAnalytics.logCryptoClaimTransferedClicked()
        launch {
            claimHandler.handle(view = view, canBeClaimed = canBeClaimed, token = token)
        }
    }

    override fun detach() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        cryptoTokensSubscription?.cancel()
        cryptoTokensSubscription = null

        pnlDataSubscription?.cancel()
        pnlDataSubscription = null
        super.detach()
    }

    override fun onAddressClicked() {
        mainScreenAnalytics.logMainScreenAddressClick()
        val fullUsername = username?.fullUsername
        val hasUserName = !fullUsername.isNullOrEmpty()
        view?.showAddressCopied(
            addressOrUsername = fullUsername ?: userPublicKey,
            stringResId = if (hasUserName) {
                R.string.wallet_username_copy_snackbar_text
            } else {
                R.string.wallet_address_copy_snackbar_text
            }
        )
    }
}

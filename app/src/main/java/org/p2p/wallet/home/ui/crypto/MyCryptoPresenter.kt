package org.p2p.wallet.home.ui.crypto

import android.content.SharedPreferences
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.core.network.ConnectionManager
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.core.token.filterTokensForWalletScreen
import org.p2p.core.utils.scaleShort
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.crypto.analytics.CryptoScreenAnalytics
import org.p2p.wallet.home.ui.crypto.handlers.BridgeClaimBundleClickHandler
import org.p2p.wallet.home.ui.crypto.mapper.MyCryptoMapper
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.tokenservice.TokenServiceCoordinator
import org.p2p.wallet.tokenservice.UserTokensState

class MyCryptoPresenter(
    private val cryptoInteractor: MyCryptoInteractor,
    private val cryptoMapper: MyCryptoMapper,
    private val connectionManager: ConnectionManager,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val cryptoScreenAnalytics: CryptoScreenAnalytics,
    private val claimHandler: BridgeClaimBundleClickHandler,
    private val sharedPreferences: SharedPreferences,
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

    private var cryptoTokensSubscription: Job? = null

    override fun attach(view: MyCryptoContract.View) {
        super.attach(view)
        prepareAndShowActionButtons()
        observeCryptoTokens()
        cryptoScreenAnalytics.logCryptoScreenOpened()
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    override fun refreshTokens() {
        launchInternetAware(connectionManager) {
            try {
                tokenServiceCoordinator.refresh()
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
        cryptoTokensSubscription = launch {
            tokenServiceCoordinator.observeUserTokens()
                .collect { handleTokenState(it) }
        }
    }

    private fun handleTokenState(newState: UserTokensState) {
        view?.showRefreshing(isRefreshing = newState.isLoading())

        when (newState) {
            is UserTokensState.Idle -> Unit
            is UserTokensState.Loading -> Unit
            is UserTokensState.Refreshing -> Unit
            is UserTokensState.Error -> {
                view?.showErrorMessage(newState.cause)
            }
            is UserTokensState.Empty -> {
                view?.showEmptyState(isEmpty = true)
                handleEmptyAccount()
            }
            is UserTokensState.Loaded -> {
                view?.showEmptyState(isEmpty = false)
                showTokensAndBalance(
                    solTokens = filterCryptoTokens(newState.solTokens),
                    ethTokens = newState.ethTokens
                )
            }
        }
    }

    private fun filterCryptoTokens(solTokens: List<Token.Active>): List<Token.Active> {
        val excludedTokens = solTokens.filterTokensForWalletScreen()
        return solTokens.minus(excludedTokens.toSet())
    }

    private fun showTokensAndBalance(solTokens: List<Token.Active>, ethTokens: List<Token.Eth>) {
        val balance = getUserBalance(solTokens)
        view?.showBalance(cryptoMapper.mapBalance(balance))
        cryptoScreenAnalytics.logUserAggregateBalanceBase(balance)

        val areZerosHidden = cryptoInteractor.areZerosHidden()
        if (ethTokens.isNotEmpty()) {
            cryptoScreenAnalytics.logCryptoClaimTransferedViewed(ethTokens.size)
        }
        val mappedItems: List<AnyCellItem> = cryptoMapper.mapToCellItems(
            tokens = solTokens,
            ethereumTokens = ethTokens,
            visibilityState = currentVisibilityState,
            isZerosHidden = areZerosHidden,
        )
        view?.showItems(mappedItems)
    }

    private fun getUserBalance(tokens: List<Token.Active>): BigDecimal {
        if (tokens.none { it.totalInUsd != null }) return BigDecimal.ZERO

        return tokens
            .mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()
    }

    private fun handleEmptyAccount() {
        cryptoScreenAnalytics.logUserAggregateBalanceBase(BigDecimal.ZERO)
        view?.showBalance(cryptoMapper.mapBalance(BigDecimal.ZERO))
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
        val buttons = listOf(ActionButton.RECEIVE_BUTTON, ActionButton.SWAP_BUTTON)
        view?.showActionButtons(buttons)
    }

    override fun onReceiveClicked() {
        cryptoScreenAnalytics.logCryptoReceiveClick()
        view?.navigateToReceive()
    }

    override fun onSwapClicked() {
        cryptoScreenAnalytics.logCryptoSwapClick()
        view?.navigateToSwap()
    }

    override fun onClaimClicked(canBeClaimed: Boolean, token: Token.Eth) {
        cryptoScreenAnalytics.logCryptoClaimTransferedClicked()
        launch {
            claimHandler.handle(view, canBeClaimed, token)
        }
    }

    override fun detach() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        super.detach()
    }
}

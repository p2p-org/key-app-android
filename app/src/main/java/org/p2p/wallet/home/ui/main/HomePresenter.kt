package org.p2p.wallet.home.ui.main

import androidx.lifecycle.LifecycleOwner
import android.content.res.Resources
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.Constants.ETH_COINGECKO_ID
import org.p2p.core.utils.Constants.ETH_SYMBOL
import org.p2p.core.utils.Constants.SOL_COINGECKO_ID
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.Constants.USDC_COINGECKO_ID
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.core.utils.Constants.USDT_COINGECKO_ID
import org.p2p.core.utils.Constants.USDT_SYMBOL
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.model.Banner
import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.model.HomeMapper
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.main.models.ViewState
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomDeeplinkManager
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.solana.SolanaNetworkObserver
import org.p2p.wallet.updates.UpdatesManager
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.utils.ellipsizeAddress

val POPULAR_TOKENS_SYMBOLS = setOf(USDC_SYMBOL, SOL_SYMBOL, ETH_SYMBOL, USDT_SYMBOL)
val POPULAR_TOKENS_COINGECKO_IDS = setOf(
    SOL_COINGECKO_ID,
    USDT_COINGECKO_ID,
    ETH_COINGECKO_ID,
    USDC_COINGECKO_ID
).map { TokenId(it) }
val TOKEN_SYMBOLS_VALID_FOR_BUY = listOf(USDC_SYMBOL, SOL_SYMBOL)

class HomePresenter(
    private val analytics: HomeAnalytics,
    private val updatesManager: UpdatesManager,
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val environmentManager: NetworkEnvironmentManager,
    private val tokenKeyProvider: TokenKeyProvider,
    private val homeElementItemMapper: HomeElementItemMapper,
    private val resources: Resources,
    private val tokensPolling: UserTokensPolling,
    private val newBuyFeatureToggle: NewBuyFeatureToggle,
    private val networkObserver: SolanaNetworkObserver,
    private val sellInteractor: SellInteractor,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val metadataInteractor: MetadataInteractor,
    private val intercomDeeplinkManager: IntercomDeeplinkManager,
    private val homeMapper: HomeMapper,
    private val ethereumInteractor: EthereumInteractor,
    private val seedPhraseProvider: SeedPhraseProvider,
) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {

    private var username: Username? = null

    init {
        val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase
        ethereumInteractor.setup(userSeedPhrase = userSeedPhrase)
        launch {
            awaitAll(
                async { networkObserver.start() },
                async { metadataInteractor.tryLoadAndSaveMetadata() }
            )
        }
    }

    override fun attach(view: HomeContract.View) {
        super.attach(view)
        launch {
            tokensPolling.shareTokenPollFlowIn(this).onEach {
                val isRefreshing = it.first.isEmpty()
                view.showRefreshing(isRefreshing)
            }.collect { (solTokens, ethTokens) ->
                state = state.copy(tokens = solTokens, ethTokens = ethTokens)
                if (solTokens.isEmpty() && ethTokens.isEmpty()) {
                    userInteractor.loadUserRates(userInteractor.loadUserTokensAndUpdateLocal())
                }
                handleUserTokensLoaded(solTokens, ethTokens)
                initializeActionButtons()
            }
        }
    }

    private var state = ViewState(areZerosHidden = settingsInteractor.areZerosHidden())

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        intercomDeeplinkManager.proceedDeeplinkIfExists()
    }

    override fun load() {
        showUserAddressAndUsername()

        updatesManager.start()
        tokensPolling.startPolling()

        val userId = username?.value ?: tokenKeyProvider.publicKey
        IntercomService.signIn(userId)

        environmentManager.addEnvironmentListener(this::class) { refreshTokens() }
    }

    private fun initializeActionButtons() {
        launch {
            val isSellFeatureToggleEnabled = sellEnabledFeatureToggle.isFeatureEnabled
            val isSellAvailable = sellInteractor.isSellAvailable()

            val buttons = mutableListOf(
                ActionButton.BUY_BUTTON,
                ActionButton.RECEIVE_BUTTON,
                ActionButton.SEND_BUTTON
            )

            if (!isSellFeatureToggleEnabled) {
                buttons += ActionButton.SWAP_BUTTON
            }

            if (isSellAvailable) {
                buttons += ActionButton.SELL_BUTTON
            }

            view?.showActionButtons(buttons)
        }
    }

    private fun showUserAddressAndUsername() {
        this.username = usernameInteractor.getUsername()
        val userAddress = username?.fullUsername ?: tokenKeyProvider.publicKey.ellipsizeAddress()
        view?.showUserAddress(userAddress)
        state = state.copy(
            username = username,
            visibilityState = VisibilityState.create(userInteractor.getHiddenTokensVisibility())
        )
    }

    override fun onAddressClicked() {
        view?.showAddressCopied(username?.fullUsername ?: tokenKeyProvider.publicKey)
    }

    override fun onBuyClicked() {
        launch {
            val tokensForBuy = userInteractor.getTokensForBuy()
            if (tokensForBuy.isEmpty()) return@launch

            if (newBuyFeatureToggle.isFeatureEnabled) {
                // this cannot be empty
                view?.showNewBuyScreen(tokensForBuy.first())
            } else {
                view?.showTokensForBuy(tokensForBuy)
            }
        }
    }

    override fun onBuyTokenClicked(token: Token) {
        if (token.tokenSymbol !in TOKEN_SYMBOLS_VALID_FOR_BUY) {
            view?.showBuyInfoScreen(token)
        } else {
            onBuyToken(token)
        }
    }

    override fun onInfoBuyTokenClicked(token: Token) {
        onBuyToken(token)
    }

    private fun onBuyToken(token: Token) {
        launch {
            val tokenToBuy = if (token.isSOL || token.isUSDC) {
                token
            } else {
                userInteractor.getSingleTokenForBuy() ?: return@launch
            }

            if (newBuyFeatureToggle.isFeatureEnabled) {
                view?.showNewBuyScreen(tokenToBuy)
            } else {
                view?.showOldBuyScreen(tokenToBuy)
            }
        }
    }

    override fun onSendClicked() {
        launch {
            val isEmptyAccount = state.tokens.all { it.isZero } && state.ethTokens.isEmpty()
            if (isEmptyAccount) {
                // this cannot be empty
                val validTokenToBuy = userInteractor.getSingleTokenForBuy() ?: return@launch
                view?.showSendNoTokens(validTokenToBuy)
            } else {
                view?.showNewSendScreen()
            }
        }
    }

    private fun handleUserTokensLoaded(
        userTokens: List<Token.Active>,
        ethTokens: List<Token.Eth>,
    ) {
        Timber.d("local tokens change arrived")
        state = state.copy(
            tokens = userTokens,
            ethTokens = ethTokens,
            username = usernameInteractor.getUsername(),
        )

        val isAccountEmpty = userTokens.all(Token.Active::isZero) && ethTokens.isEmpty()
        when {
            isAccountEmpty -> {
                view?.showEmptyState(isEmpty = true)
                handleEmptyAccount()
            }
            userTokens.isNotEmpty() -> {
                view?.showEmptyState(isEmpty = false)
                showTokensAndBalance()
            }
        }
    }

    private fun handleEmptyAccount() {
        launch {
            val tokensForBuy =
                userInteractor.findMultipleTokenData(POPULAR_TOKENS_SYMBOLS.toList())
                    .sortedBy { tokenToBuy -> POPULAR_TOKENS_SYMBOLS.indexOf(tokenToBuy.tokenSymbol) }

            val homeBannerItem = HomeBannerItem(
                id = R.id.home_banner_top_up,
                titleTextId = R.string.main_banner_title,
                subtitleTextId = R.string.main_banner_subtitle,
                buttonTextId = R.string.main_banner_button,
                drawableRes = R.drawable.ic_main_banner,
                backgroundColorRes = R.color.bannerBackgroundColor
            )
            view?.showEmptyViewData(
                listOf(
                    homeBannerItem,
                    resources.getString(R.string.main_popular_tokens_header),
                    *tokensForBuy.toTypedArray()
                )
            )
            logBalance(BigDecimal.ZERO)

            view?.showBalance(homeMapper.mapBalance(BigDecimal.ZERO))
        }
    }

    override fun refreshTokens() {
        launch {
            try {
                view?.showRefreshing(isRefreshing = true)
                tokensPolling.refresh()
            } catch (cancelled: CancellationException) {
                Timber.i("Loading tokens job cancelled")
            } catch (error: Throwable) {
                Timber.e(error, "Error refreshing user tokens")
                view?.showErrorMessage(error)
            } finally {
                view?.showRefreshing(isRefreshing = false)
            }
        }
    }

    override fun toggleTokenVisibility(token: Token.Active) {
        launch {
            val handleDefaultVisibility = { token: Token.Active ->
                if (settingsInteractor.areZerosHidden() && token.isZero) {
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

            userInteractor.setTokenHidden(
                mintAddress = token.mintAddress,
                visibility = newVisibility.stringValue
            )

            val updatedTokens = userInteractor.getUserTokens()
            handleUserTokensLoaded(updatedTokens, state.ethTokens)
        }
    }

    override fun toggleTokenVisibilityState() {
        state = state.run { copy(visibilityState = visibilityState.toggle()) }
        userInteractor.setHiddenTokensVisibility(state.visibilityState.isVisible)

        showTokensAndBalance()
    }

    override fun clearTokensCache() {
        state = state.copy(tokens = emptyList())
    }

    private fun showTokensAndBalance() {
        launch {
            val balance = getUserBalance()

            if (balance != null) {
                view?.showBalance(homeMapper.mapBalance(balance))
            } else {
                view?.showBalance(null)
            }

            logBalance(balance)

            /* Mapping elements according to visibility settings */
            val areZerosHidden = settingsInteractor.areZerosHidden()
            val mappedTokens = homeElementItemMapper.mapToItems(
                tokens = state.tokens,
                ethereumTokens = state.ethTokens,
                visibilityState = state.visibilityState,
                isZerosHidden = areZerosHidden
            )

            view?.showTokens(mappedTokens, areZerosHidden)
        }
    }

    private fun logBalance(balance: BigDecimal?) {
        val hasPositiveBalance = balance != null && balance.isMoreThan(BigDecimal.ZERO)
        analytics.logUserHasPositiveBalanceProperty(hasPositiveBalance)
        analytics.logUserAggregateBalanceProperty(balance.orZero())
    }
//
//    private fun loadTokenRates(loadedTokens: List<Token.Active>) {
//        ratesJob?.cancel()
//        ratesJob = launchSupervisor {
//            try {
//                view?.showBalance(homeMapper.mapRateSkeleton())
//                userInteractor.loadUserRates(loadedTokens)
//                val updatedTokens = async { userInteractor.getUserTokens() }
//                val ethereumState = async { getEthereumState() }
//                handleUserTokensLoaded(updatedTokens.await(), ethereumState.await())
//            } catch (e: Throwable) {
//                Timber.e(e, "Error loading token rates")
//                view?.showBalance(cellModel = null)
//                view?.showUiKitSnackBar(messageResId = R.string.error_token_rates)
//            }
//        }
//    }

    private fun getUserBalance(): BigDecimal? {
        val tokens = state.tokens

        if (tokens.none { it.totalInUsd != null }) return null

        return tokens
            .mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()
    }

    private fun getBanners(): List<Banner> {
        val usernameExists = state.username != null

        val feedbackBanner = Banner(
            R.string.home_feedback_banner_option,
            R.string.main_feedback_banner_action,
            R.drawable.ic_feedback,
            R.color.backgroundBannerSecondary,
            isSingle = usernameExists
        )

        return if (usernameExists) {
            listOf(feedbackBanner)
        } else {
            val usernameBanner = Banner(
                R.string.home_username_banner_option,
                R.string.main_username_banner_action,
                R.drawable.ic_username,
                R.color.backgroundBanner
            )
            listOf(
                usernameBanner,
                feedbackBanner
            )
        }
    }

    override fun onProfileClick() {
        if (usernameInteractor.isUsernameExist()) {
            view?.navigateToProfile()
        } else {
            view?.navigateToReserveUsername()
        }
    }

    override fun updateTokensIfNeeded() {
        if (state.areZerosHidden != settingsInteractor.areZerosHidden()) {
            refreshTokens()
            state = state.copy(areZerosHidden = settingsInteractor.areZerosHidden())
        }
    }

    override fun detach() {
        environmentManager.removeEnvironmentListener(this::class)
        super.detach()
    }
}

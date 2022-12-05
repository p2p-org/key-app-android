package org.p2p.wallet.home.ui.main

import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.analytics.HomeAnalytics
import org.p2p.wallet.home.model.Banner
import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenVisibility
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.solana.SolanaNetworkObserver
import org.p2p.wallet.updates.UpdatesManager
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Constants.BTC_SYMBOL
import org.p2p.wallet.utils.Constants.ETH_SYMBOL
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.utils.Constants.USDC_SYMBOL
import org.p2p.wallet.utils.Constants.USDT_SYMBOL
import org.p2p.wallet.utils.appendWhitespace
import org.p2p.wallet.utils.ellipsizeAddress
import org.p2p.wallet.utils.isMoreThan
import org.p2p.wallet.utils.scaleShort
import timber.log.Timber
import java.math.BigDecimal
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val POPULAR_TOKENS = setOf(USDC_SYMBOL, SOL_SYMBOL, BTC_SYMBOL, ETH_SYMBOL, USDT_SYMBOL)

private val TOKENS_VALID_FOR_BUY = setOf(SOL_SYMBOL, USDC_SYMBOL)
private val LOAD_TOKENS_DELAY_MS = 1.toDuration(DurationUnit.SECONDS).inWholeMilliseconds

class HomePresenter(
    private val analytics: HomeAnalytics,
    private val updatesManager: UpdatesManager,
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val environmentManager: NetworkEnvironmentManager,
    private val tokenKeyProvider: TokenKeyProvider,
    private val homeElementItemMapper: HomeElementItemMapper,
    private val resourcesProvider: ResourcesProvider,
    private val tokensPolling: UserTokensPolling,
    private val newBuyFeatureToggle: NewBuyFeatureToggle,
    private val networkObserver: SolanaNetworkObserver,
    private val metadataInteractor: MetadataInteractor
) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {

    private var fallbackUsdcTokenForBuy: Token? = null
    private var username: Username? = null

    init {
        // TODO maybe we can find better place to start this service
        launch {
            awaitAll(
                async { fallbackUsdcTokenForBuy = userInteractor.getTokensForBuy(listOf(USDC_SYMBOL)).firstOrNull() },
                async { networkObserver.start() },
                async { metadataInteractor.tryLoadAndSaveMetadata() }
            )
        }
    }

    private data class ViewState(
        val tokens: List<Token.Active> = emptyList(),
        val visibilityState: VisibilityState = VisibilityState.Hidden,
        val username: Username? = null,
        val areZerosHidden: Boolean
    )

    private var state = ViewState(
        areZerosHidden = settingsInteractor.areZerosHidden()
    )

    override fun load() {
        showUserAddressAndUsername()

        updatesManager.start()

        if (state.tokens.isEmpty()) {
            initialLoadTokens()
        } else {
            handleUserTokensLoaded(state.tokens)
        }
        startPollingForTokens()

        val userId = username?.value ?: tokenKeyProvider.publicKey
        IntercomService.signIn(userId)

        environmentManager.addEnvironmentListener(this::class) { refreshTokens() }
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
        // example result: "test-android.key 4vwfPYdvv9vkX5mTC6BBh4cQcWFTQ7Q7WR42JyTfZwi7"
        val userDataToCopy = buildString {
            username?.fullUsername?.let {
                append(it)
                appendWhitespace()
            }
            append(tokenKeyProvider.publicKey)
        }
        view?.showAddressCopied(userDataToCopy)
    }

    override fun onBuyClicked() {
        launch {
            val tokensForBuy = userInteractor.getTokensForBuy(TOKENS_VALID_FOR_BUY.toList())
            view?.showTokensForBuy(tokensForBuy, newBuyFeatureToggle.value)
        }
    }

    override fun onBuyTokenClicked(token: Token) {
        if (token.tokenSymbol !in TOKENS_VALID_FOR_BUY) {
            view?.showBuyInfoScreen(token)
        } else {
            onBuyToken(token)
        }
    }

    override fun onInfoBuyTokenClicked(token: Token) {
        onBuyToken(token)
    }

    private fun onBuyToken(token: Token) {
        val tokenToBuy: Token? = if (token.tokenSymbol !in TOKENS_VALID_FOR_BUY) {
            fallbackUsdcTokenForBuy
        } else {
            token
        }
        if (tokenToBuy == null) {
            Timber.i("Token to buy: token=$token")
            Timber.e(IllegalArgumentException("No fallback USDC token to buy found"))
            return
        }

        if (newBuyFeatureToggle.value) {
            view?.showNewBuyScreen(tokenToBuy)
        } else {
            view?.showOldBuyScreen(tokenToBuy)
        }
    }

    private fun handleUserTokensLoaded(userTokens: List<Token.Active>) {
        launch {
            Timber.d("local tokens change arrived")
            state = state.copy(
                tokens = userTokens,
                username = usernameInteractor.getUsername(),
            )

            val isAccountEmpty = userTokens.all { it.isZero }
            when {
                isAccountEmpty -> {
                    handleEmptyAccount()
                }
                userTokens.isNotEmpty() -> {
                    view?.showEmptyState(isEmpty = false)
                    showTokensAndBalance()
                }
            }
        }
    }

    private fun handleEmptyAccount() {
        launch {
            view?.showEmptyState(isEmpty = true)

            val tokensForBuy =
                userInteractor.getTokensForBuy(POPULAR_TOKENS.toList())
                    .sortedBy { tokenToBuy -> POPULAR_TOKENS.indexOf(tokenToBuy.tokenSymbol) }
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
                    resourcesProvider.getString(R.string.main_popular_tokens_header),
                    *tokensForBuy.toTypedArray()
                )
            )
            logBalance(BigDecimal.ZERO)
        }
    }

    override fun refreshTokens() {
        launch {
            try {
                view?.showRefreshing(isRefreshing = true)

                val loadedTokens = userInteractor.loadUserTokensAndUpdateLocal(fetchPrices = true)
                handleUserTokensLoaded(loadedTokens)
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
        val balance = getUserBalance()
        view?.showBalance(balance)

        logBalance(balance)

        /* Mapping elements according to visibility settings */
        val areZerosHidden = settingsInteractor.areZerosHidden()
        val mappedTokens = homeElementItemMapper.mapToItems(
            tokens = state.tokens, visibilityState = state.visibilityState, isZerosHidden = areZerosHidden
        )

        view?.showTokens(mappedTokens, areZerosHidden)
    }

    private fun logBalance(balance: BigDecimal) {
        val hasPositiveBalance = balance.isMoreThan(BigDecimal.ZERO)
        analytics.logUserHasPositiveBalanceProperty(hasPositiveBalance)
        analytics.logUserAggregateBalanceProperty(balance)
    }

    private fun initialLoadTokens() {
        launch {
            try {
                Timber.d("initial token loading")
                view?.showRefreshing(isRefreshing = true)

                delay(LOAD_TOKENS_DELAY_MS)

                val loadedTokens = userInteractor.loadUserTokensAndUpdateLocal(fetchPrices = true)
                handleUserTokensLoaded(loadedTokens)
            } catch (cancelled: CancellationException) {
                Timber.i("Cancelled initial tokens remote update")
            } catch (error: Throwable) {
                Timber.e(error, "Error initial loading tokens")
            } finally {
                view?.showRefreshing(isRefreshing = false)
            }
        }
    }

    private fun startPollingForTokens() {
        tokensPolling.startPolling(scope = this, onTokensLoaded = ::handleUserTokensLoaded)
    }

    private fun getUserBalance(): BigDecimal =
        state.tokens
            .mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()

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

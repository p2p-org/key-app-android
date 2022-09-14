package org.p2p.wallet.home.ui.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Banner
import org.p2p.wallet.home.model.HomeBannerItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenVisibility
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.updates.UpdatesManager
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Constants.REN_BTC_SYMBOL
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.utils.Constants.USDC_SYMBOL
import org.p2p.wallet.utils.ellipsizeAddress
import org.p2p.wallet.utils.scaleShort
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

private val POLLING_DELAY_MS = TimeUnit.SECONDS.toMillis(10)
private const val BANNER_START_INDEX = 2
private val TOKENS_VALID_FOR_BUY = setOf(SOL_SYMBOL, USDC_SYMBOL)
private val POPULAR_TOKENS = setOf(SOL_SYMBOL, USDC_SYMBOL, REN_BTC_SYMBOL)

class HomePresenter(
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val updatesManager: UpdatesManager,
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val environmentManager: NetworkEnvironmentManager,
    private val tokenKeyProvider: TokenKeyProvider,
    private val homeElementItemMapper: HomeElementItemMapper,
    private val resourcesProvider: ResourcesProvider,
    private val newBuyFeatureToggle: NewBuyFeatureToggle,
) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {

    private data class ViewState(
        val tokens: List<Token.Active> = emptyList(),
        val visibilityState: VisibilityState = VisibilityState.Hidden,
        val username: Username? = null,
        val areZerosHidden: Boolean
    )

    private var state = ViewState(
        areZerosHidden = settingsInteractor.areZerosHidden()
    )

    private var userTokensFlowJob: Job? = null

    override fun attach(view: HomeContract.View) {
        super.attach(view)

        view.showEmptyState(isEmpty = true)

        view.showUserAddress(tokenKeyProvider.publicKey.ellipsizeAddress())

        updatesManager.start()

        state = state.copy(
            username = usernameInteractor.getUsername(),
            visibilityState = VisibilityState.create(userInteractor.getHiddenTokensVisibility())
        )

        if (state.tokens.isEmpty()) {
            initialLoadTokens()
        } else {
            startPollingForTokens()
        }

        val userId = usernameInteractor.getUsername()?.username ?: tokenKeyProvider.publicKey
        IntercomService.signIn(userId)

        environmentManager.addEnvironmentListener(this::class) { refreshTokens() }
    }

    override fun onAddressClicked() {
        view?.showAddressCopied(tokenKeyProvider.publicKey)
    }

    override fun onBuyClicked() {
        launch {
            val tokensForBuy = userInteractor.getTokensForBuy(TOKENS_VALID_FOR_BUY.toList())
            view?.showTokensForBuy(tokensForBuy, newBuyFeatureToggle.value)
        }
    }

    override fun onBuyTokenClicked(token: Token) {
        if (newBuyFeatureToggle.value) {
            view?.showNewBuyScreen(token)
        } else {
            view?.showOldBuyScreen(token)
        }
    }

    override fun subscribeToUserTokensFlow() {
        userTokensFlowJob?.cancel()
        userTokensFlowJob = launch {
            userInteractor.getUserTokensFlow()
                // emits two times when local tokens updated: with [] and actual list - strange
                .collect { updatedTokens ->
                    Timber.d("local tokens change arrived")
                    state = state.copy(
                        tokens = updatedTokens,
                        username = usernameInteractor.getUsername(),
                    )

                    val isAccountEmpty = updatedTokens.run { size == 1 && first().isSOL && first().isZero }
                    when {
                        isAccountEmpty -> {
                            val tokensForBuyOrReceive = userInteractor.getTokensForBuy(POPULAR_TOKENS.toList())
                            view?.showEmptyState(isEmpty = true)
                            view?.showEmptyViewData(
                                listOf(
                                    HomeBannerItem(
                                        id = R.id.home_banner_top_up,
                                        titleTextId = R.string.main_banner_title,
                                        subtitleTextId = R.string.main_banner_subtitle,
                                        buttonTextId = R.string.main_banner_button,
                                        drawableRes = R.drawable.ic_banner_image,
                                        backgroundColorRes = R.color.bannerBackgroundColor
                                    ),
                                    resourcesProvider.getString(R.string.main_popular_tokens_header)
                                ) + tokensForBuyOrReceive
                            )
                        }
                        updatedTokens.isNotEmpty() -> {
                            view?.showEmptyState(isEmpty = false)
                            showTokensAndBalance()
                        }
                    }
                }
        }
    }

    override fun refreshTokens() {
        launch {
            view?.showRefreshing(isRefreshing = true)

            runCatching { userInteractor.loadUserTokensAndUpdateLocal(fetchPrices = true) }
                .onSuccess { Timber.d("refreshing tokens is success") }
                .onFailure { handleUserTokensUpdateFailure(it) }

            view?.showRefreshing(isRefreshing = false)
        }
    }

    private fun handleUserTokensUpdateFailure(error: Throwable) {
        if (error is CancellationException) {
            Timber.d("Loading tokens job cancelled")
        } else {
            Timber.e(error, "Error loading user data")
            view?.showErrorMessage(error)
        }
    }

    override fun toggleTokenVisibility(token: Token.Active) {
        launch {
            val visibility = when (token.visibility) {
                TokenVisibility.SHOWN -> TokenVisibility.HIDDEN
                TokenVisibility.HIDDEN -> TokenVisibility.SHOWN
                TokenVisibility.DEFAULT -> if (settingsInteractor.areZerosHidden() && token.isZero) {
                    TokenVisibility.SHOWN
                } else {
                    TokenVisibility.HIDDEN
                }
            }

            userInteractor.setTokenHidden(token.mintAddress, visibility.stringValue)
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

        /* Mapping elements according to visibility settings */
        val areZerosHidden = settingsInteractor.areZerosHidden()
        val mappedTokens = homeElementItemMapper.mapToItems(
            tokens = state.tokens,
            visibilityState = state.visibilityState,
            isZerosHidden = areZerosHidden
        )

        view?.showTokens(mappedTokens, areZerosHidden)
    }

    private fun initialLoadTokens() {
        launch {
            Timber.d("initial token loading")
            view?.showRefreshing(isRefreshing = true)
            // We are waiting when tokenlist.json is being parsed and saved into the memory
            delay(1000L)
            kotlin.runCatching { userInteractor.loadUserTokensAndUpdateLocal(fetchPrices = true) }
                .onSuccess {
                    Timber.d("Successfully initial loaded tokens")
                }
                .onFailure {
                    if (it is CancellationException) {
                        Timber.i("Cancelled initial tokens remote update")
                    } else {
                        Timber.e(it, "Error initial loading tokens from remote")
                    }
                }

            view?.showRefreshing(isRefreshing = false)
            startPollingForTokens()
        }
    }

    private fun startPollingForTokens() {
        launch {
            try {
                while (true) {
                    delay(POLLING_DELAY_MS)
                    loadTokensOnPolling()
                }
            } catch (e: CancellationException) {
                Timber.w("Cancelled tokens remote update")
            } catch (e: Throwable) {
                Timber.e(e, "Error refreshing tokens")
            }
        }
    }

    private suspend fun loadTokensOnPolling() {
        val isPollingEnabled = inAppFeatureFlags.isPollingEnabled.featureValue
        if (isPollingEnabled) {
            userInteractor.loadUserTokensAndUpdateLocal(fetchPrices = false)
            Timber.d("Successfully auto-updated loaded tokens")
        } else {
            Timber.d("Skipping tokens auto-update")
        }
    }

    private fun getUserBalance(): BigDecimal =
        state.tokens
            .mapNotNull { it.totalInUsd }
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
        if (usernameInteractor.usernameExists()) {
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
}

package org.p2p.wallet.home.ui.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.AppFeatureFlags
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.ActionButtonsView
import org.p2p.wallet.home.model.Banner
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenVisibility
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.updates.UpdatesManager
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.scaleShort
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

private val POLLING_DELAY_MS = TimeUnit.SECONDS.toMillis(10)
private const val BANNER_START_INDEX = 2
private val TOKENS_VALID_FOR_BUY = setOf("SOL", "USDC")
private const val BALANCE_CURRENCY = "USD"

class HomePresenter(
    private val appFeatureFlags: AppFeatureFlags,
    private val updatesManager: UpdatesManager,
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val environmentManager: EnvironmentManager,
    private val tokenKeyProvider: TokenKeyProvider,
    private val homeElementItemMapper: HomeElementItemMapper
) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {

    private data class ViewState(
        val tokens: List<Token.Active>,
        val visibilityState: VisibilityState?,
        val username: Username?
    ) {
        companion object {
            val EMPTY = ViewState(tokens = emptyList(), visibilityState = null, username = null)
        }

        val actualVisibilityState: VisibilityState = when (visibilityState) {
            is VisibilityState.Hidden, null -> VisibilityState.Hidden
            is VisibilityState.Visible -> VisibilityState.Visible
        }
    }

    private var presenterState = ViewState.EMPTY

    private var userTokensFlowJob: Job? = null

    override fun attach(view: HomeContract.View) {
        super.attach(view)

        view.showActions(
            listOf(
                ActionButtonsView.ActionButton(R.string.main_buy, R.drawable.ic_plus),
                ActionButtonsView.ActionButton(R.string.main_receive, R.drawable.ic_receive_simple),
                ActionButtonsView.ActionButton(R.string.main_send, R.drawable.ic_send_medium),
                ActionButtonsView.ActionButton(R.string.main_swap, R.drawable.ic_swap_medium)
            )
        )

        updatesManager.start()

        presenterState = presenterState.copy(username = usernameInteractor.getUsername())

        if (presenterState.tokens.isEmpty()) {
            initialLoadTokens()
        } else {
            startPollingForTokens()
        }

        IntercomService.signIn(tokenKeyProvider.publicKey)

        environmentManager.addEnvironmentListener(this::class) {
            refreshTokenAndPrices()
        }
    }

    override fun onBuyClicked() {
        launch {
            val tokensForBuy = userInteractor.getTokensForBuy(TOKENS_VALID_FOR_BUY.toList())
            view?.showTokensForBuy(tokensForBuy)
        }
    }

    override fun subscribeToUserTokensFlow() {
        userTokensFlowJob?.cancel()
        userTokensFlowJob = launch {
            userInteractor.getUserTokensFlow()
                // emits two times when local tokens updated: with [] and actual list - strange
                .collect { updatedTokens ->
                    Timber.d("local tokens change arrived")
                    presenterState = presenterState.copy(tokens = updatedTokens)

                    val isAccountEmpty = updatedTokens.run { size == 1 && first().isSOL && first().isZero }
                    when {
                        isAccountEmpty -> {
                            view?.showEmptyState(isEmpty = true)
                        }
                        updatedTokens.isNotEmpty() -> {
                            view?.showEmptyState(isEmpty = false)
                            showTokensAndBalance()
                        }
                    }
                }
        }
    }

    override fun refreshTokenAndPrices() {
        launch {
            view?.showRefreshing(isRefreshing = true)

            runCatching { userInteractor.loadTokenPrices(BALANCE_CURRENCY) }
                .onSuccess { Timber.d("refreshing prices is success") }
                .onFailure { onTokenPricesLoadFailure(it) }
                .getOrNull()
                ?.runCatching { userInteractor.loadUserTokensAndUpdateLocal() }
                ?.onSuccess { Timber.d("refreshing tokens is success") }
                ?.onFailure { handleUserTokensUpdateFailure(it) }

            view?.showRefreshing(isRefreshing = false)
        }
    }

    private fun onTokenPricesLoadFailure(error: Throwable) {
        Timber.e(error, "Error loading token prices")
        view?.showErrorSnackBar(error.message ?: error.localizedMessage)
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
                TokenVisibility.DEFAULT -> if (settingsInteractor.isZerosHidden() && token.isZero) {
                    TokenVisibility.SHOWN
                } else {
                    TokenVisibility.HIDDEN
                }
            }

            userInteractor.setTokenHidden(token.mintAddress, visibility.stringValue)
        }
    }

    override fun toggleTokenVisibilityState() {
        presenterState = presenterState.run { copy(visibilityState = visibilityState?.toggle()) }

        showTokensAndBalance()
    }

    override fun clearTokensCache() {
        presenterState = presenterState.copy(tokens = emptyList())
    }

    private fun showTokensAndBalance() {
        Timber.d("showing tokens on screen")
        val balance = getUserBalance()
        view?.showBalance(balance, presenterState.username)

        /* Mapping elements according to visibility settings */
        val isZerosHidden = settingsInteractor.isZerosHidden()
        val actualState = presenterState.actualVisibilityState
        val mappedTokens = buildList {
            addAll(
                homeElementItemMapper.mapToItem(
                    tokens = presenterState.tokens,
                    visibilityState = presenterState.actualVisibilityState,
                    isZerosHidden = isZerosHidden
                )
            )

            // Adding banners to the main list
            val banners = getBanners()
            if (this.size > BANNER_START_INDEX) {
                add(BANNER_START_INDEX, HomeElementItem.Banners(banners))
            } else {
                add(HomeElementItem.Banners(banners))
            }
        }

        view?.showTokens(mappedTokens, isZerosHidden, actualState)
    }

    private fun initialLoadTokens() {
        launch {
            Timber.d("initial token loading")
            view?.showRefreshing(isRefreshing = true)
            // We are waiting when tokenlist.json is being parsed and saved into the memory
            delay(1000L)
            kotlin.runCatching { userInteractor.loadUserTokensAndUpdateLocal() }
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
        val isPollingEnabled = appFeatureFlags.isPollingEnabled
        if (isPollingEnabled) {
            userInteractor.loadUserTokensAndUpdateLocal()
            Timber.d("Successfully auto-updated loaded tokens")
        } else {
            Timber.d("Skipping tokens auto-update")
        }
    }

    private fun getUserBalance(): BigDecimal =
        presenterState.tokens
            .mapNotNull { it.totalInUsd }
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()

    private fun getBanners(): List<Banner> {
        val usernameBanner = Banner(
            R.string.main_username_banner_option,
            R.string.main_username_banner_action,
            R.drawable.ic_username,
            R.color.backgroundBanner
        )

        val usernameExists = presenterState.username != null
        val feedbackBanner = Banner(
            R.string.main_feedback_banner_option,
            R.string.main_feedback_banner_action,
            R.drawable.ic_feedback,
            R.color.backgroundBannerSecondary,
            isSingle = usernameExists
        )

        return if (usernameExists) {
            listOf(feedbackBanner)
        } else {
            listOf(usernameBanner, feedbackBanner)
        }
    }
}

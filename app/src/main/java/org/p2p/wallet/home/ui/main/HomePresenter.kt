package org.p2p.wallet.home.ui.main

import android.content.SharedPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.widget.ActionButtonsView
import org.p2p.wallet.debugdrawer.KEY_POLLING_ENABLED
import org.p2p.wallet.home.model.Banner
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenVisibility
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.updates.UpdatesManager
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.scaleShort
import timber.log.Timber
import java.math.BigDecimal

private const val DELAY_MS = 10000L
private const val BANNER_START_INDEX = 2

class HomePresenter(
    private val updatesManager: UpdatesManager,
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val sharedPreferences: SharedPreferences,
    private val tokenKeyProvider: TokenKeyProvider

) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {

    companion object {
        private const val BALANCE_CURRENCY = "USD"
    }

    private var state: VisibilityState? = null

    private val tokens = mutableListOf<Token.Active>()
    private val tokensValidForBuy = listOf("SOL", "USDC")

    private var username: Username? = null

    private var collectJob: Job? = null

    private val actions = mutableListOf(
        ActionButtonsView.ActionButton(R.string.main_buy, R.drawable.ic_plus),
        ActionButtonsView.ActionButton(R.string.main_receive, R.drawable.ic_receive_simple),
        ActionButtonsView.ActionButton(R.string.main_send, R.drawable.ic_send_medium),
        ActionButtonsView.ActionButton(R.string.main_swap, R.drawable.ic_swap_medium)
    )

    override fun attach(view: HomeContract.View) {
        super.attach(view)
        view.showActions(actions)
        updatesManager.start()
        loadData()
        username = usernameInteractor.getUsername()
        IntercomService.signIn(tokenKeyProvider.publicKey) {}
    }

    override fun onBuyClicked() {
        launch {
            val tokensForBuy = userInteractor.getTokensForBuy(tokensValidForBuy)
            view?.showTokensForBuy(tokensForBuy)
        }
    }

    override fun collectData() {
        collectJob?.cancel()
        collectJob = launch {
            userInteractor.getUserTokensFlow().collect { updatedTokens ->
                when {
                    isEmptyAccount(updatedTokens) -> {
                        view?.showEmptyState(true)
                    }

                    updatedTokens.isNotEmpty() -> {
                        view?.showEmptyState(false)
                        tokens.clear()
                        tokens += updatedTokens
                        showTokens(updatedTokens.toMutableList())
                    }
                }
            }
        }
    }

    override fun refresh() {
        view?.showRefreshing(true)
        launch {
            try {
                fetchRates()
                userInteractor.loadUserTokensAndUpdateData()
            } catch (e: CancellationException) {
                Timber.d("Loading tokens job cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading user data")
                view?.showErrorMessage(e)
            } finally {
                view?.showRefreshing(false)
            }
        }
    }

    private suspend fun fetchRates() {
        try {
            userInteractor.loadTokenPrices(BALANCE_CURRENCY)
        } catch (e: Throwable) {
            Timber.e(e, "Error loading token prices")
            view?.showErrorSnackBar(e.message ?: e.localizedMessage)
        }
    }

    override fun toggleVisibility(token: Token.Active) {
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

    override fun toggleVisibilityState() {
        state = when (state) {
            is VisibilityState.Visible -> VisibilityState.Hidden
            else -> VisibilityState.Visible
        }

        showTokens(tokens)
    }

    override fun clearCache() {
        tokens.clear()
    }

    private fun showTokens(tokens: MutableList<Token.Active>) {
        val balance = sumBalance(tokens)
        view?.showBalance(balance, username)

        /* Mapping elements according to visibility settings */
        val isZerosHidden = settingsInteractor.isZerosHidden()
        val actualState = when (state) {
            is VisibilityState.Hidden, null -> VisibilityState.Hidden
            is VisibilityState.Visible -> VisibilityState.Visible
        }
        val mappedTokens = mapTokens(tokens, isZerosHidden, actualState)

        /* Adding banners to the main list */
        val banners = getBanners()
        if (mappedTokens.size > BANNER_START_INDEX) {
            mappedTokens.add(BANNER_START_INDEX, HomeElementItem.Banners(banners))
        } else {
            mappedTokens.add(HomeElementItem.Banners(banners))
        }

        view?.showTokens(mappedTokens, isZerosHidden, actualState)
    }

    private fun loadData() {
        if (tokens.isNotEmpty()) {
            startPolling()
            return
        }

        launch {
            try {
                view?.showRefreshing(true)
                /* We are waiting when tokenlist.json is being parsed and saved into the memory */
                delay(1000L)
                userInteractor.loadUserTokensAndUpdateData()
                Timber.d("Successfully loaded tokens")
            } catch (e: CancellationException) {
                Timber.w("Cancelled tokens remote update")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading tokens from remote")
            } finally {
                view?.showRefreshing(false)
                startPolling()
            }
        }
    }

    private fun startPolling() {
        launch {
            try {
                while (true) {
                    delay(DELAY_MS)
                    val isPollingEnabled = sharedPreferences.getBoolean(KEY_POLLING_ENABLED, true)
                    if (isPollingEnabled) {
                        userInteractor.loadUserTokensAndUpdateData()
                        Timber.d("Successfully updated loaded tokens")
                    } else {
                        Timber.d("Skipping tokens auto-update")
                    }

                    view?.showRefreshing(false)
                }
            } catch (e: CancellationException) {
                Timber.w("Cancelled tokens remote update")
            } catch (e: Throwable) {
                Timber.e(e, "Error refreshing tokens")
            }
        }
    }

    private fun sumBalance(tokens: List<Token.Active>): BigDecimal =
        tokens
            .mapNotNull { it.totalInUsd }
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()

    private fun mapTokens(
        tokens: MutableList<Token.Active>,
        isZerosHidden: Boolean,
        state: VisibilityState
    ): MutableList<HomeElementItem> =
        tokens
            .map { token ->
                if (token.isSOL) return@map HomeElementItem.Shown(token)

                when (token.visibility) {
                    TokenVisibility.SHOWN -> HomeElementItem.Shown(token)
                    TokenVisibility.HIDDEN -> HomeElementItem.Hidden(token, state)
                    TokenVisibility.DEFAULT -> if (isZerosHidden && token.isZero) {
                        HomeElementItem.Hidden(token, state)
                    } else {
                        HomeElementItem.Shown(token)
                    }
                }
            }
            .toMutableList()

    private fun getBanners(): List<Banner> {
        val usernameExists = username != null

        val usernameBanner = Banner(
            R.string.main_username_banner_option,
            R.string.main_username_banner_action,
            R.drawable.ic_username,
            R.color.backgroundBanner
        )

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

    private fun isEmptyAccount(updatedTokens: List<Token.Active>) =
        updatedTokens.size == 1 && updatedTokens.first().isSOL && updatedTokens.first().isZero
}

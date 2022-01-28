package org.p2p.wallet.main.ui.main

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.debugdrawer.KEY_POLLING_ENABLED
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.TokenItem
import org.p2p.wallet.main.model.TokenVisibility
import org.p2p.wallet.main.model.VisibilityState
import org.p2p.wallet.rpc.interactor.TransactionAmountInteractor
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.updates.UpdatesManager
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.scaleShort
import timber.log.Timber
import java.math.BigDecimal
import kotlin.properties.Delegates

private const val DELAY_MS = 10000L
private const val KEY_BANNER_VISIBLE = "KEY_BANNER_VISIBLE"

class MainPresenter(
    private val updatesManager: UpdatesManager,
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val amountInteractor: TransactionAmountInteractor,
    private val sharedPreferences: SharedPreferences
) : BasePresenter<MainContract.View>(), MainContract.Presenter {

    companion object {
        private const val BALANCE_CURRENCY = "USD"
    }

    private var state: VisibilityState? = null

    private var balance: BigDecimal by Delegates.observable(BigDecimal.ZERO) { _, _, newValue ->
        view?.showBalance(newValue)
    }

    private var tokens: List<Token.Active> by Delegates.observable(emptyList()) { _, _, newValue ->
        balance = mapBalance(newValue)
        val isZerosHidden = settingsInteractor.isZerosHidden()
        val actualState = when (state) {
            is VisibilityState.Hidden, null ->
                VisibilityState.Hidden(newValue.count { it.isDefinitelyHidden(isZerosHidden) })
            is VisibilityState.Visible ->
                VisibilityState.Visible
        }
        val mappedTokens = mapTokens(newValue, isZerosHidden, actualState)

        view?.showTokens(mappedTokens, isZerosHidden, actualState)
        view?.showChart(newValue)
    }

    private var isVisibleBanner: Boolean = true

    private var collectJob: Job? = null

    override fun attach(view: MainContract.View) {
        super.attach(view)
        updatesManager.start()
        loadData()
        checkUsername()
    }

    override fun collectData() {
        collectJob?.cancel()
        collectJob = launch {
            userInteractor.getUserTokensFlow().collect { updatedTokens ->
                if (updatedTokens.isNotEmpty()) tokens = updatedTokens
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
            view?.showSnackbarError(e.message ?: e.localizedMessage)
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
            is VisibilityState.Visible -> {
                val isZerosHidden = settingsInteractor.isZerosHidden()
                val count = tokens.count { it.isDefinitelyHidden(isZerosHidden) }
                VisibilityState.Hidden(count)
            }
            else -> VisibilityState.Visible
        }

        val old = ArrayList(tokens)
        tokens = old
    }

    override fun clearCache() {
        tokens = emptyList()
    }

    override fun hideUsernameBanner(forever: Boolean) {
        if (forever) {
            sharedPreferences.edit { putBoolean(KEY_BANNER_VISIBLE, false) }
        }
        isVisibleBanner = false
        view?.showUsernameBanner(false)
    }

    private fun loadData() {
        if (tokens.isNotEmpty()) {
            startPolling()
            return
        }

        launch {
            try {
                view?.showLoading(true)
                /* We are waiting when tokenlist.json is being parsed and saved into the memory */
                delay(1000L)
                userInteractor.loadUserTokensAndUpdateData()
                Timber.d("Successfully loaded tokens")
            } catch (e: CancellationException) {
                Timber.w("Cancelled tokens remote update")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading tokens from remote")
            } finally {
                view?.showLoading(false)
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

    private fun checkUsername() {
        val isNotHidden = sharedPreferences.getBoolean(KEY_BANNER_VISIBLE, true)
        val isBannerVisible = isNotHidden && !usernameInteractor.usernameExists() && isVisibleBanner
        view?.showUsernameBanner(isBannerVisible)
    }

    private fun mapBalance(tokens: List<Token.Active>): BigDecimal =
        tokens
            .mapNotNull { it.totalInUsd }
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()

    private fun mapTokens(tokens: List<Token.Active>, isZerosHidden: Boolean, state: VisibilityState): List<TokenItem> =
        tokens.map {
            if (it.isSOL) return@map TokenItem.Shown(it)

            when (it.visibility) {
                TokenVisibility.SHOWN -> TokenItem.Shown(it)
                TokenVisibility.HIDDEN -> TokenItem.Hidden(it, state)
                TokenVisibility.DEFAULT -> if (isZerosHidden && it.isZero) {
                    TokenItem.Hidden(it, state)
                } else {
                    TokenItem.Shown(it)
                }
            }
        }
}
package org.p2p.wallet.main.ui.main

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.main.model.TokenItem
import org.p2p.wallet.main.model.VisibilityState
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.TokenVisibility
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.scaleShort
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.BigDecimal
import kotlin.properties.Delegates

private const val DELAY_MS = 10000L

class MainPresenter(
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor
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

    private var collectJob: Job? = null

    override fun attach(view: MainContract.View) {
        super.attach(view)
        loadData()
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
                withContext(Dispatchers.Default) { userInteractor.loadTokenPrices(BALANCE_CURRENCY) }
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
                    userInteractor.loadUserTokensAndUpdateData()
                    Timber.d("Successfully updated loaded tokens")
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error loading tokens from remote")
            }
        }
    }

    private fun mapBalance(tokens: List<Token.Active>): BigDecimal =
        tokens
            .map { it.price }
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
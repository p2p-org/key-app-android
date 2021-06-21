package com.p2p.wallet.main.ui.main

import com.p2p.wallet.amount.scaleShort
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.settings.interactor.SettingsInteractor
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.TokenVisibility
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
    settingsInteractor: SettingsInteractor
) : BasePresenter<MainContract.View>(), MainContract.Presenter {

    companion object {
        private const val BALANCE_CURRENCY = "USD"
    }

    private var balance: BigDecimal by Delegates.observable(BigDecimal.ZERO) { _, _, newValue ->
        view?.showBalance(newValue)
    }

    private var tokens: List<Token> by Delegates.observable(emptyList()) { _, _, newValue ->
        balance = mapBalance(newValue)
        val mappedTokens = mapTokens(newValue, settingsInteractor.isZerosHidden())

        view?.showChart(newValue)
        view?.showTokens(mappedTokens)
        view?.showLoading(false)
        view?.showRefreshing(false)
    }

    override fun attach(view: MainContract.View) {
        super.attach(view)
        loadTokensFromRemote()
    }

    override fun loadData() {
        launch {
            try {
                view?.showLoading(true)
                userInteractor.getTokensFlow().collect {
                    if (it.isNotEmpty()) tokens = it
                }
            } catch (e: CancellationException) {
                Timber.d("Loading tokens job cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading user data")
                view?.showErrorMessage(e)
            }
        }
    }

    override fun refresh() {
        view?.showRefreshing(true)
        GlobalScope.launch {
            try {
                withContext(Dispatchers.Default) { userInteractor.loadTokenPrices(BALANCE_CURRENCY) }
                userInteractor.loadTokens()
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

    override fun startPolling() {
        launch {
            try {
                while (true) {
                    delay(DELAY_MS)
                    userInteractor.loadTokens()
                    Timber.d("Successfully updated loaded tokens")
                }
            } catch (e: Throwable) {
                Timber.e(e, "Error loading tokens from remote")
            }
        }
    }

    override fun toggleVisibility(token: Token) {
        launch {
            val visibility = when (token.visibility) {
                TokenVisibility.SHOWN,
                TokenVisibility.DEFAULT -> TokenVisibility.HIDDEN
                else -> TokenVisibility.SHOWN
            }
            userInteractor.setTokenHidden(token.publicKey, visibility.stringValue)
        }
    }

    private fun loadTokensFromRemote() {
        if (tokens.isNotEmpty()) return

        launch {
            try {
                view?.showHorizontalLoading(true)
                userInteractor.getTokenDataFlow().collect {
                    val isDataLoaded = it.isNotEmpty()
                    if (isDataLoaded) {
                        Timber.tag("MAIN").d("Global token data is loaded. Starting fetching user's data")
                        userInteractor.loadTokens()
                        view?.showHorizontalLoading(false)
                    }
                }
                Timber.d("Successfully loaded tokens")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading tokens from remote")
            }
        }
    }

    private fun mapBalance(tokens: List<Token>): BigDecimal =
        tokens
            .map { it.price }
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()

    private fun mapTokens(tokens: List<Token>, isZerosHidden: Boolean): List<TokenItem> = when {
        isZerosHidden -> {
            val hiddenTokens = tokens.filter { it.isHidden || (it.isZero && it.visibility == TokenVisibility.DEFAULT) }
            val hiddenGroup = listOf(TokenItem.Group(hiddenTokens))
            val result = tokens.filter { it.isSOL || !it.isHidden && !it.isZero }.map { TokenItem.Shown(it) }
            if (hiddenTokens.isEmpty()) result else result + hiddenGroup
        }
        else -> {
            val hiddenTokens = tokens.filter { it.isHidden }
            val hiddenGroup = listOf(TokenItem.Group(hiddenTokens))
            val result = tokens.filter { it.isSOL || !it.isHidden }.map { TokenItem.Shown(it) }
            if (hiddenTokens.isEmpty()) result else result + hiddenGroup
        }
    }
}
package com.p2p.wallet.main.ui.main

import com.p2p.wallet.amount.scaleShort
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.settings.interactor.SettingsInteractor
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.BigDecimal
import kotlin.properties.Delegates

class MainPresenter(
    private val userInteractor: UserInteractor,
    settingsInteractor: SettingsInteractor
) : BasePresenter<MainContract.View>(), MainContract.Presenter {

    companion object {
        private const val BALANCE_CURRENCY = "USD"
    }

    private var balance: BigDecimal by Delegates.observable(BigDecimal.ZERO) { _, oldValue, newValue ->
        if (newValue != oldValue) view?.showBalance(newValue)
    }

    private var tokens: List<Token> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        balance = mapBalance(newValue)
        val mappedTokens = mapTokens(newValue, settingsInteractor.isHidden())

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
                userInteractor.getTokensFlow().collect { tokens = it }
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

    override fun toggleVisibility(token: Token) {
        launch {
            userInteractor.setTokenHidden(token.publicKey, !token.isHidden)
        }
    }

    private fun loadTokensFromRemote() {
        if (tokens.isNotEmpty()) return

        launch {
            try {
                userInteractor.loadTokens()
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

    private fun mapTokens(tokens: List<Token>, isHidden: Boolean): List<TokenItem> =
        if (isHidden) {
            val hiddenTokens = tokens.filter { it.isHidden }
            val hiddenGroup = listOf(TokenItem.Group(hiddenTokens))
            val result = tokens.filter { !it.isHidden }.map { TokenItem.Shown(it) }
            if (hiddenTokens.isEmpty()) result else result + hiddenGroup
        } else {
            tokens.map { TokenItem.Shown(it) }
        }
}
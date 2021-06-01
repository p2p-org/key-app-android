package com.p2p.wallet.main.ui.main

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
import java.math.RoundingMode
import kotlin.properties.Delegates

class MainPresenter(
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor
) : BasePresenter<MainContract.View>(), MainContract.Presenter {

    companion object {
        private const val BALANCE_CURRENCY = "USD"
    }

    private val isHidden = settingsInteractor.isHidden()

    private var tokens: List<Token> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (newValue == oldValue) {
            view?.showLoading(false)
            view?.showRefreshing(false)
            return@observable
        }

        val balance = mapBalance(newValue)
        val mappedTokens = mapTokens(newValue, isHidden)

        view?.showData(mappedTokens, balance)
        view?.showLoading(false)
        view?.showRefreshing(false)
    }

    override fun attach(view: MainContract.View) {
        super.attach(view)
        loadTokensFromRemote()
    }

    override fun loadData() {
        view?.showLoading(true)
        launch {
            try {
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
            .setScale(2, RoundingMode.HALF_EVEN)

    private fun mapTokens(tokens: List<Token>, isHidden: Boolean): List<TokenItem> =
        when {
            tokens.size == 1 ->
                tokens.map { TokenItem.Shown(it) }
            isHidden && tokens.size > 1 -> {
                val hiddenTokens = tokens.filter { it.isZero && !it.isSOL }
                if (hiddenTokens.isEmpty()) {
                    tokens.mapNotNull { if (!it.isZero || it.isSOL) TokenItem.Shown(it) else null }
                } else {
                    val hiddenGroup = TokenItem.Group(hiddenTokens)
                    tokens.mapNotNull { if (!it.isZero || it.isSOL) TokenItem.Shown(it) else null } + listOf(hiddenGroup)
                }
            }
            else -> {
                tokens.map { TokenItem.Shown(it) }
            }
        }
}
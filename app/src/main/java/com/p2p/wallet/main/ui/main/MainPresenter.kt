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

class MainPresenter(
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor
) : BasePresenter<MainContract.View>(), MainContract.Presenter {

    companion object {
        private const val BALANCE_CURRENCY = "USD"
    }

    override fun attach(view: MainContract.View) {
        super.attach(view)
        loadTokensFromRemote()
    }

    override fun loadData() {
        view?.showLoading(true)
        launch {
            try {
                val isHidden = settingsInteractor.isHidden()
                userInteractor.getTokensFlow().collect { tokens ->
                    if (tokens.isNotEmpty()) {
                        val balance = mapBalance(tokens)
                        val mappedTokens = mapTokens(tokens, isHidden)

                        view?.showData(mappedTokens, balance)
                        view?.showLoading(false)
                        view?.showRefreshing(false)
                    }
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

    private fun loadTokensFromRemote() {
        launch {
            try {
                val tokens = userInteractor.getTokens()
                if (tokens.isNotEmpty()) {
                    Timber.d("Tokens are already loaded, skipping refresh")
                    return@launch
                }
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
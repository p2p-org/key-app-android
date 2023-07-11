package org.p2p.wallet.tokenservice

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants.SOL_SYMBOL
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.core.utils.Constants.USDT_SYMBOL
import org.p2p.core.utils.Constants.WETH_SYMBOL
import org.p2p.wallet.home.events.EthereumTokensLoader
import org.p2p.wallet.home.events.SolanaTokensLoader
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.tokenservice.model.EthTokenLoadState
import org.p2p.wallet.tokenservice.model.SolanaTokenLoadState

/**
 * This class is responsible for launching/refreshing Solana and Eth loaders
 * Each feature Presenter which needs user tokens should use this class to get/observe them
 * */

private val POPULAR_TOKENS_SYMBOLS: Set<String> = setOf(USDC_SYMBOL, SOL_SYMBOL, WETH_SYMBOL, USDT_SYMBOL)

class TokenServiceCoordinator(
    private val solanaTokensLoader: SolanaTokensLoader,
    private val ethereumTokensLoader: EthereumTokensLoader,
    private val homeInteractor: HomeInteractor,
    private val appScope: AppScope
) {

    /**
     * Token Service will be needed in different screens and may have 2 or more collectors.
     * Therefore, we are using SharedFlow
     * */
    private val tokensState = MutableSharedFlow<UserTokenState>()

    init {
        solanaTokensLoader.observeState()
            .combine(ethereumTokensLoader.observeState()) { solState, ethState ->
                mapTokenState(solState, ethState)
            }
            .launchIn(appScope)
    }

    fun observeUserTokens(): SharedFlow<UserTokenState> = tokensState.asSharedFlow()

    fun start() {
        appScope.launch {
            solanaTokensLoader.load()
            ethereumTokensLoader.loadIfEnabled()
        }
    }

    fun refresh() {
        appScope.launch {
            solanaTokensLoader.refresh()
            ethereumTokensLoader.refreshIfEnabled()
        }
    }

    private suspend fun mapTokenState(solState: SolanaTokenLoadState, ethState: EthTokenLoadState): UserTokenState =
        when (solState) {
            is SolanaTokenLoadState.Idle -> UserTokenState.Idle
            is SolanaTokenLoadState.Loading -> UserTokenState.Loading
            is SolanaTokenLoadState.Refreshing -> UserTokenState.Refreshing
            is SolanaTokenLoadState.Error -> UserTokenState.Error(solState.throwable)
            is SolanaTokenLoadState.Loaded -> handleLoadedState(solState, ethState)
        }

    private suspend fun handleLoadedState(
        solState: SolanaTokenLoadState.Loaded,
        ethState: EthTokenLoadState
    ): UserTokenState {
        val solTokens = solState.tokens
        val ethTokens = if (ethState is EthTokenLoadState.Loaded) ethState.tokens else emptyList()

        if (solTokens.all(Token.Active::isZero) && ethTokens.isEmpty()) {
            val tokens = loadTokensForEmptyState()
            return UserTokenState.Empty(tokens)
        }

        val isSellAvailable = homeInteractor.isSellFeatureAvailable()
        return UserTokenState.Loaded(solTokens, ethTokens, isSellAvailable)
    }

    private suspend fun loadTokensForEmptyState(): List<Token.Other> =
        homeInteractor.findMultipleTokenData(POPULAR_TOKENS_SYMBOLS.toList())
            .sortedBy { tokenToBuy ->
                POPULAR_TOKENS_SYMBOLS.indexOf(tokenToBuy.tokenSymbol)
            }
}

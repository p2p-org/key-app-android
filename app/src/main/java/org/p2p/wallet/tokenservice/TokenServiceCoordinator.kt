package org.p2p.wallet.tokenservice

import timber.log.Timber
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.token.Token
import org.p2p.wallet.home.events.EthereumTokensLoader
import org.p2p.wallet.home.events.SolanaTokensLoader
import org.p2p.wallet.tokenservice.model.EthTokenLoadState
import org.p2p.wallet.tokenservice.model.SolanaTokenLoadState

/**
 * This class is responsible for launching/refreshing Solana and Eth loaders
 * Each feature Presenter which needs user tokens should use this class to get/observe them
 * */

private const val TAG = "TokenServiceCoordinator"

class TokenServiceCoordinator(
    private val solanaTokensLoader: SolanaTokensLoader,
    private val ethereumTokensLoader: EthereumTokensLoader,
    private val appScope: AppScope
) {
    /**
     * Token Service will be needed in different screens and may have 2 or more collectors.
     * Therefore, we are using SharedFlow
     * */
    private val tokensState = MutableSharedFlow<UserTokensState>(replay = 1)
    private val lastState = MutableStateFlow<UserTokensState>(UserTokensState.Idle)

    init {
        combine(
            flow = solanaTokensLoader.observeState(),
            flow2 = ethereumTokensLoader.observeState(),
            transform = ::mapTokenState
        )
            .onEach {
                lastState.emit(it)
                tokensState.emit(it)
            }
            .launchIn(appScope)
    }

    fun start() {
        Timber.tag(TAG).i("Starting Token Service loaders")
        appScope.launch {
            solanaTokensLoader.load()
            ethereumTokensLoader.loadIfEnabled()
        }
    }

    fun refresh() {
        Timber.tag(TAG).i("Refreshing Token Service loaders")
        appScope.launch {
            solanaTokensLoader.refresh()
            ethereumTokensLoader.refreshIfEnabled()
        }
    }

    fun observeUserTokens(): SharedFlow<UserTokensState> = tokensState.asSharedFlow()

    fun observeLastState(): StateFlow<UserTokensState> = lastState.asStateFlow()

    suspend fun getUserTokens(): List<Token.Active> {
        return solanaTokensLoader.getUserTokens()
    }

    suspend fun getUserSolToken(): Token.Active? = getUserTokens().find { it.isSOL }

    private fun mapTokenState(solState: SolanaTokenLoadState, ethState: EthTokenLoadState): UserTokensState =
        when (solState) {
            is SolanaTokenLoadState.Idle -> UserTokensState.Idle
            is SolanaTokenLoadState.Loading -> UserTokensState.Loading
            is SolanaTokenLoadState.Refreshing -> UserTokensState.Refreshing
            is SolanaTokenLoadState.Error -> UserTokensState.Error(solState.throwable)
            is SolanaTokenLoadState.Loaded -> handleLoadedState(solState, ethState)
        }

    private fun handleLoadedState(
        solState: SolanaTokenLoadState.Loaded,
        ethState: EthTokenLoadState
    ): UserTokensState {
        val solTokens = solState.tokens
        val ethTokens = if (ethState is EthTokenLoadState.Loaded) {
            ethState.tokens
        } else {
            ethereumTokensLoader.getLastLoadedTokens()
        }
        if (solTokens.all(Token.Active::isZero) && ethTokens.isEmpty()) {
            return UserTokensState.Empty
        }

        return UserTokensState.Loaded(solTokens, ethTokens)
    }
}

package org.p2p.wallet.tokenservice

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.token.Token
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.home.events.EthereumTokensLoader
import org.p2p.wallet.home.events.SolanaTokensLoader
import org.p2p.wallet.user.interactor.UserTokensInteractor

class TokenService(
    private val userTokensInteractor: UserTokensInteractor,
    private val ethereumInteractor: EthereumInteractor,
    private val solanaTokensLoader: SolanaTokensLoader,
    private val ethereumTokensLoader: EthereumTokensLoader,
    private val appScope: AppScope,
    private val loadStateHelper: TokenServiceLoadStateHelper
) {

    private val tokensState = MutableStateFlow<TokenState>(TokenState.Loading)

    init {
        appScope.launch {
            userTokensInteractor.getUserTokensFlow()
                .combine(ethereumInteractor.observeTokensFlow()) { solTokens, ethTokens ->
                    mapToTokenState(solTokens, ethTokens)
                }.collect(tokensState)
        }
    }

    fun observeUserTokens(): Flow<TokenState> {
        return tokensState
    }

    fun onStart() {
        appScope.launch {
            solanaTokensLoader.onStart()
            ethereumTokensLoader.onStart().takeIf { ethereumTokensLoader.isEnabled() }
        }
    }

    fun onRefresh() {
        appScope.launch {
            solanaTokensLoader.onRefresh()
            ethereumTokensLoader.onRefresh().takeIf { ethereumTokensLoader.isEnabled() }
        }
    }

    private fun mapToTokenState(solTokens: List<Token.Active>, ethTokens: List<Token.Eth>): TokenState {
        val solTokensState = solanaTokensLoader.getCurrentLoadState()
        val ethTokensState = ethereumTokensLoader.getCurrentLoadState()
        val isLoading = loadStateHelper.isLoading(solTokensState, ethTokensState)
        val isRefreshing = loadStateHelper.isRefreshing(solTokensState, ethTokensState)
        return when {
            isLoading -> TokenState.Loading
            isRefreshing -> TokenState.Refreshing
            else -> TokenState.Loaded(solTokens, ethTokens)
        }
    }
}

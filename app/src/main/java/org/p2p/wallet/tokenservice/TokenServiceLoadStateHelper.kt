package org.p2p.wallet.tokenservice

object TokenServiceLoadStateHelper {

    fun isLoading(solTokensState: TokenLoadState, ethTokensState: TokenLoadState): Boolean {
        return when {
            solTokensState == TokenLoadState.LOADING && ethTokensState == TokenLoadState.IDLE -> true
            solTokensState == TokenLoadState.LOADING && ethTokensState == TokenLoadState.LOADING -> true
            solTokensState == TokenLoadState.LOADING || ethTokensState == TokenLoadState.LOADING -> true
            else -> false
        }
    }

    fun isRefreshing(solTokensState: TokenLoadState, ethTokensState: TokenLoadState): Boolean {
        return when {
            solTokensState == TokenLoadState.REFRESHING && ethTokensState == TokenLoadState.IDLE -> true
            solTokensState == TokenLoadState.REFRESHING && ethTokensState == TokenLoadState.REFRESHING -> true
            solTokensState == TokenLoadState.REFRESHING || ethTokensState == TokenLoadState.REFRESHING -> true
            else -> false
        }
    }
}

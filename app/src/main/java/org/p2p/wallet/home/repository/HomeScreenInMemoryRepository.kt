package org.p2p.wallet.home.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.p2p.wallet.home.events.UserTokensLoader

class HomeScreenInMemoryRepository : HomeScreenLocalRepository {
    private val userTokensState = MutableSharedFlow<UserTokensLoader.UserTokensState>()
    private val refreshState = MutableSharedFlow<Boolean>()

    override fun getUserTokensStateFlow(): SharedFlow<UserTokensLoader.UserTokensState> {
        return userTokensState.asSharedFlow()
    }

    override suspend fun setUserTokensState(value: UserTokensLoader.UserTokensState) {
        userTokensState.emit(value)
    }

    override suspend fun setRefreshState(isRefreshing: Boolean) {
        refreshState.emit(isRefreshing)
    }

    override fun getHomeScreenRefreshStateFlow(): SharedFlow<Boolean> {
        return refreshState
    }
}

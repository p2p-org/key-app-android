package org.p2p.wallet.home.repository

import kotlinx.coroutines.flow.SharedFlow
import org.p2p.wallet.home.events.UserTokensLoader

interface HomeScreenLocalRepository {
    fun getUserTokensStateFlow(): SharedFlow<UserTokensLoader.UserTokensState>
    suspend fun setUserTokensState(value: UserTokensLoader.UserTokensState)

    suspend fun setRefreshState(isRefreshing: Boolean)
    fun getHomeScreenRefreshStateFlow(): SharedFlow<Boolean>
}

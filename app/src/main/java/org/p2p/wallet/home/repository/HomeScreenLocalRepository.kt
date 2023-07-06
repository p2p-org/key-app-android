package org.p2p.wallet.home.repository

import kotlinx.coroutines.flow.SharedFlow
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.events.HomeScreenTokensLoader
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner

interface HomeScreenLocalRepository {
    fun getUserTokensStateFlow(): SharedFlow<HomeScreenTokensLoader.UserTokensState>
    suspend fun setUserTokensState(value: HomeScreenTokensLoader.UserTokensState)

    suspend fun setRefreshState(isRefreshing: Boolean)
    fun getHomeScreenRefreshStateFlow(): SharedFlow<Boolean>

    suspend fun setActionButtons(actionButtons: List<ActionButton>)
    fun getHomeScreenActionButtonsFlow(): SharedFlow<List<ActionButton>>

    fun getStrigaUserStatusBannerFlow(): SharedFlow<StrigaKycStatusBanner>
    suspend fun setStrigaUserStatusBanner(banner: StrigaKycStatusBanner)
}

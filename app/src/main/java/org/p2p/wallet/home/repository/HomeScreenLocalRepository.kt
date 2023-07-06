package org.p2p.wallet.home.repository

import java.math.BigDecimal
import kotlinx.coroutines.flow.SharedFlow
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.events.HomeScreenStateLoader
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner

interface HomeScreenLocalRepository {
    fun getUserTokensStateFlow(): SharedFlow<HomeScreenStateLoader.HomeScreenState>
    suspend fun setUserTokensState(value: HomeScreenStateLoader.HomeScreenState)

    suspend fun setRefreshState(isRefreshing: Boolean)
    fun getHomeScreenRefreshStateFlow(): SharedFlow<Boolean>

    suspend fun setActionButtons(actionButtons: List<ActionButton>)
    fun getHomeScreenActionButtonsFlow(): SharedFlow<List<ActionButton>>

    fun getStrigaUserStatusBannerFlow(): SharedFlow<StrigaKycStatusBanner>
    suspend fun setStrigaUserStatusBanner(banner: StrigaKycStatusBanner)

    suspend fun setUsername(username: String)
    fun getUsernameFlow(): SharedFlow<String>

    fun observeUserBalance(): SharedFlow<BigDecimal?>
    suspend fun setUserBalance(userBalance: BigDecimal?)
}

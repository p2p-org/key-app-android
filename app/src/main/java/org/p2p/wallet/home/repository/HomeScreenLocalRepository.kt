package org.p2p.wallet.home.repository

import java.math.BigDecimal
import kotlinx.coroutines.flow.StateFlow
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.events.HomeScreenStateLoader
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner

interface HomeScreenLocalRepository {
    fun getUserTokensStateFlow(): StateFlow<HomeScreenStateLoader.HomeScreenState?>
    suspend fun setUserTokensState(value: HomeScreenStateLoader.HomeScreenState)

    suspend fun setRefreshState(isRefreshing: Boolean)
    fun getHomeScreenRefreshStateFlow(): StateFlow<Boolean>

    suspend fun setActionButtons(actionButtons: List<ActionButton>)
    fun getHomeScreenActionButtonsFlow(): StateFlow<List<ActionButton>>

    fun getStrigaUserStatusBannerFlow(): StateFlow<StrigaKycStatusBanner?>
    suspend fun setStrigaUserStatusBanner(banner: StrigaKycStatusBanner)

    suspend fun setUsername(username: String)
    fun getUsernameFlow(): StateFlow<String?>

    fun observeUserBalance(): StateFlow<BigDecimal?>
    suspend fun setUserBalance(userBalance: BigDecimal?)
}

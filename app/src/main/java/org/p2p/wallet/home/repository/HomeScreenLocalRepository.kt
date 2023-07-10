package org.p2p.wallet.home.repository

import java.math.BigDecimal
import kotlinx.coroutines.flow.SharedFlow
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.state.HomeScreenState
import org.p2p.wallet.home.state.HomeScreenStateObserver
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner

interface HomeScreenLocalRepository {
    fun getHomeScreenSharedFlow(): SharedFlow<HomeScreenState?>
    suspend fun setHomeScreenState(value: HomeScreenState)

    suspend fun setRefreshState(isRefreshing: Boolean)
    fun getHomeScreenRefreshSharedFlow(): SharedFlow<Boolean>

    suspend fun setActionButtons(actionButtons: List<ActionButton>)
    fun getHomeScreenActionButtonsFlow(): SharedFlow<List<ActionButton>>

    fun getStrigaUserStatusBannerFlow(): SharedFlow<StrigaKycStatusBanner>
    suspend fun setStrigaUserStatusBanner(banner: StrigaKycStatusBanner)

    suspend fun setUsername(username: String)
    fun getUsernameFlow(): SharedFlow<String?>

    fun observeUserBalance(): SharedFlow<BigDecimal?>
    suspend fun setUserBalance(userBalance: BigDecimal?)
}

package org.p2p.wallet.home.repository

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.state.HomeScreenState
import org.p2p.wallet.home.state.HomeScreenStateObserver
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner

class HomeScreenInMemoryRepository : HomeScreenLocalRepository {
    private val homeScreenState = MutableStateFlow<HomeScreenState?>(null)

    private val refreshState = MutableSharedFlow<Boolean>()
    private val actionButtonState = MutableSharedFlow<List<ActionButton>>()
    private val strigaUserStatusState = MutableSharedFlow<StrigaKycStatusBanner>()
    private val userBalanceState = MutableSharedFlow<BigDecimal?>()
    private val usernameState = MutableSharedFlow<String?>()

    override fun getHomeScreenSharedFlow(): SharedFlow<HomeScreenState?> {
        return homeScreenState
    }

    override suspend fun setHomeScreenState(value: HomeScreenState) {
        homeScreenState.emit(value)
    }

    override suspend fun setRefreshState(isRefreshing: Boolean) {
        refreshState.emit(isRefreshing)
    }

    override fun getHomeScreenRefreshSharedFlow(): SharedFlow<Boolean> {
        return refreshState
    }

    override suspend fun setActionButtons(actionButtons: List<ActionButton>) {
        actionButtonState.emit(actionButtons)
    }

    override fun getHomeScreenActionButtonsFlow(): SharedFlow<List<ActionButton>> {
        return actionButtonState
    }

    override fun getStrigaUserStatusBannerFlow(): SharedFlow<StrigaKycStatusBanner> {
        return strigaUserStatusState
    }

    override suspend fun setStrigaUserStatusBanner(banner: StrigaKycStatusBanner) {
        strigaUserStatusState.emit(banner)
    }

    override suspend fun setUsername(username: String) {
        usernameState.emit(username)
    }

    override fun getUsernameFlow(): SharedFlow<String?> {
        return usernameState
    }

    override fun observeUserBalance(): SharedFlow<BigDecimal?> {
        return userBalanceState
    }

    override suspend fun setUserBalance(userBalance: BigDecimal?) {
        userBalanceState.emit(userBalance)
    }
}

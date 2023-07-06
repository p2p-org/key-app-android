package org.p2p.wallet.home.repository

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.events.HomeScreenStateLoader
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner

class HomeScreenInMemoryRepository : HomeScreenLocalRepository {
    private val userTokensState = MutableSharedFlow<HomeScreenStateLoader.HomeScreenState>()
    private val refreshState = MutableSharedFlow<Boolean>()
    private val actionButtonState = MutableSharedFlow<List<ActionButton>>()
    private val strigaUserStatusState = MutableSharedFlow<StrigaKycStatusBanner>()
    private val userBalanceState = MutableSharedFlow<BigDecimal?>()
    private val usernameState = MutableSharedFlow<String>()

    override fun getUserTokensStateFlow(): SharedFlow<HomeScreenStateLoader.HomeScreenState> {
        return userTokensState.asSharedFlow()
    }

    override suspend fun setUserTokensState(value: HomeScreenStateLoader.HomeScreenState) {
        userTokensState.emit(value)
    }

    override suspend fun setRefreshState(isRefreshing: Boolean) {
        refreshState.emit(isRefreshing)
    }

    override fun getHomeScreenRefreshStateFlow(): SharedFlow<Boolean> {
        return refreshState.asSharedFlow()
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

    override fun getUsernameFlow(): SharedFlow<String> {
        return usernameState
    }

    override fun observeUserBalance(): SharedFlow<BigDecimal?> {
        return userBalanceState
    }

    override suspend fun setUserBalance(userBalance: BigDecimal?) {
        userBalanceState.emit(userBalance)
    }
}

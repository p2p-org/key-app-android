package org.p2p.wallet.home.repository

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.p2p.core.utils.emptyString
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.events.HomeScreenStateLoader
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner

class HomeScreenInMemoryRepository : HomeScreenLocalRepository {
    private val userTokensState = MutableStateFlow<HomeScreenStateLoader.HomeScreenState?>(null)
    private val refreshState = MutableStateFlow<Boolean>(false)
    private val actionButtonState = MutableStateFlow<List<ActionButton>>(emptyList())
    private val strigaUserStatusState = MutableStateFlow<StrigaKycStatusBanner?>(null)
    private val userBalanceState = MutableStateFlow<BigDecimal?>(null)
    private val usernameState = MutableStateFlow<String>(emptyString())

    override fun getUserTokensStateFlow(): StateFlow<HomeScreenStateLoader.HomeScreenState?> {
        return userTokensState
    }

    override suspend fun setUserTokensState(value: HomeScreenStateLoader.HomeScreenState) {
        userTokensState.emit(value)
    }

    override suspend fun setRefreshState(isRefreshing: Boolean) {
        refreshState.emit(isRefreshing)
    }

    override fun getHomeScreenRefreshStateFlow(): StateFlow<Boolean> {
        return refreshState
    }

    override suspend fun setActionButtons(actionButtons: List<ActionButton>) {
        actionButtonState.emit(actionButtons)
    }

    override fun getHomeScreenActionButtonsFlow(): StateFlow<List<ActionButton>> {
        return actionButtonState
    }

    override fun getStrigaUserStatusBannerFlow(): StateFlow<StrigaKycStatusBanner?> {
        return strigaUserStatusState
    }

    override suspend fun setStrigaUserStatusBanner(banner: StrigaKycStatusBanner) {
        strigaUserStatusState.emit(banner)
    }

    override suspend fun setUsername(username: String) {
        usernameState.emit(username)
    }

    override fun getUsernameFlow(): StateFlow<String?> {
        return usernameState
    }

    override fun observeUserBalance(): StateFlow<BigDecimal?> {
        return userBalanceState
    }

    override suspend fun setUserBalance(userBalance: BigDecimal?) {
        userBalanceState.emit(userBalance)
    }
}

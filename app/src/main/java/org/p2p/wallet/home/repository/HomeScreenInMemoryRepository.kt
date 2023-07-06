package org.p2p.wallet.home.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.events.HomeScreenTokensLoader
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner

class HomeScreenInMemoryRepository : HomeScreenLocalRepository {
    private val userTokensState = MutableSharedFlow<HomeScreenTokensLoader.UserTokensState>()
    private val refreshState = MutableSharedFlow<Boolean>()
    private val actionButtonState = MutableSharedFlow<List<ActionButton>>()
    private val strigaUserStatusState = MutableSharedFlow<StrigaKycStatusBanner>()

    override fun getUserTokensStateFlow(): SharedFlow<HomeScreenTokensLoader.UserTokensState> {
        return userTokensState.asSharedFlow()
    }

    override suspend fun setUserTokensState(value: HomeScreenTokensLoader.UserTokensState) {
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
}

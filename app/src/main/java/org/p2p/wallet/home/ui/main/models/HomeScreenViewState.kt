package org.p2p.wallet.home.ui.main.models

import org.p2p.core.token.Token
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken

data class HomeScreenViewState(
    val tokens: List<Token.Active> = emptyList(),
    val ethTokens: List<Token.Eth> = emptyList(),
    val strigaClaimableTokens: List<StrigaClaimableToken> = emptyList(),
    val visibilityState: VisibilityState = VisibilityState.Hidden,
    val username: Username? = null,
    val areZerosHidden: Boolean,
    val state: LoadingState = LoadingState.INIT_LOADING,
    val strigaKycStatusBanner: StrigaKycStatusBanner? = null,
    val isStrigaKycBannerLoading: Boolean = false
)

enum class LoadingState {
    REFRESHING,
    INIT_LOADING
}

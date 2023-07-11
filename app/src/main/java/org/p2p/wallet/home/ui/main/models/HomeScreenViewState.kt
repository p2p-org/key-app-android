package org.p2p.wallet.home.ui.main.models

import org.p2p.core.token.Token
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken

data class HomeScreenViewState(
    val tokens: List<Token.Active> = emptyList(),
    val ethTokens: List<Token.Eth> = emptyList(),
    val strigaClaimableTokens: List<StrigaClaimableToken> = emptyList(),
    val visibilityState: VisibilityState = VisibilityState.Hidden,
    val areZerosHidden: Boolean,
    val strigaKycStatusBanner: StrigaKycStatusBanner? = null,
    val isStrigaKycBannerLoading: Boolean = false
)

package org.p2p.wallet.home.state

import org.p2p.core.token.Token
import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.wallet.models.StrigaClaimableToken

sealed interface State {
    data class SolTokens(val tokens: List<Token.Active> = emptyList()) : State
    data class EthTokens(val tokens: List<Token.Eth> = emptyList()) : State
    data class StrigaTokens(val tokens: List<StrigaClaimableToken>) : State
    data class StrigaStatusBanner(val banner: StrigaKycStatusBanner) : State
}

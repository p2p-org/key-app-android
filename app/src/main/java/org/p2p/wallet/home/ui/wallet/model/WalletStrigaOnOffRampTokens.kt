package org.p2p.wallet.home.ui.wallet.model

import org.p2p.wallet.striga.offramp.models.StrigaOffRampToken
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampToken

data class WalletStrigaOnOffRampTokens(
    val offRampTokens: List<StrigaOffRampToken> = emptyList(),
    val onRampTokens: List<StrigaOnRampToken> = emptyList(),
) {
    val hasTokens: Boolean
        get() = offRampTokens.isNotEmpty() || onRampTokens.isNotEmpty()
}

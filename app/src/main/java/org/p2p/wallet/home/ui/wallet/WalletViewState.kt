package org.p2p.wallet.home.ui.wallet

import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampToken

data class WalletViewState(
    val strigaOnRampTokens: List<StrigaOnRampToken> = emptyList(),
    val strigaBanner: StrigaKycStatusBanner? = null,
)

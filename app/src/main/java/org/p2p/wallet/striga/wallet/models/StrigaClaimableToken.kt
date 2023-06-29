package org.p2p.wallet.striga.wallet.models

import java.math.BigDecimal
import org.p2p.core.token.Token

data class StrigaClaimableToken(
    val claimableAmount: BigDecimal,
    val tokenDetails: Token
)

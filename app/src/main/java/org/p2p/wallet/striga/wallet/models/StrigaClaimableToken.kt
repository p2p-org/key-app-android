package org.p2p.wallet.striga.wallet.models

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId

data class StrigaClaimableToken(
    val claimableAmount: BigDecimal,
    val tokenDetails: Token,
    val walletId: StrigaWalletId,
    val accountId: StrigaAccountId,
)

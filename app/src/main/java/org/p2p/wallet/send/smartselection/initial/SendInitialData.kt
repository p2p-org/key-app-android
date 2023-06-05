package org.p2p.wallet.send.smartselection.initial

import java.math.BigDecimal
import org.p2p.core.token.Token

data class SendInitialData(
    val selectedToken: Token.Active,
    val inputAmount: BigDecimal
)

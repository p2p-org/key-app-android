package org.p2p.wallet.newsend.model.smartselection

import java.math.BigDecimal
import org.p2p.core.token.Token

class NoFeesData(
    val sourceToken: Token.Active,
    val initialAmount: BigDecimal?
)

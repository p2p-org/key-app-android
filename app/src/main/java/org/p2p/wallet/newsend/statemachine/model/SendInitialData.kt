package org.p2p.wallet.newsend.statemachine.model

import java.math.BigDecimal
import org.p2p.core.token.SolAddress

sealed interface SendInitialData {

    data class Bridge(
        val initialToken: SendToken.Bridge,
        val initialAmount: BigDecimal?,
        val recipient: SolAddress,
    )
}

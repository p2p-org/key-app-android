package org.p2p.wallet.newsend.statemachine.model

import java.math.BigDecimal

sealed interface SendInitialData {

    /**
     * support bridge send
     */
    data class Common(
        val initialToken: SendToken.Common,
        val initialAmount: BigDecimal?,
    )
}

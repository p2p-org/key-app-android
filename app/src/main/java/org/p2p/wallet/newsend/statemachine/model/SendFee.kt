package org.p2p.wallet.newsend.statemachine.model

import java.math.BigDecimal

sealed interface SendFee {

    companion object {
        fun mockCommon() = Common(BigDecimal.valueOf(0.4665), System.currentTimeMillis())
    }

    /**
     * support bridge send
     */
    data class Common(
        val fee: BigDecimal,
        val updateTimeMs: Long
    ) : SendFee
}

package org.p2p.wallet.newsend.statemachine.model

import java.math.BigDecimal

sealed interface SendFee {

    companion object {
        fun mockBridge() = Bridge(BigDecimal.valueOf(0.4665), System.currentTimeMillis())
    }

    data class Bridge(
        val fee: BigDecimal,
        val updateTimeMs: Long
    ) : SendFee
}

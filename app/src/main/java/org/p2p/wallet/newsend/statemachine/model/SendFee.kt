package org.p2p.wallet.newsend.statemachine.model

import org.p2p.wallet.bridge.send.model.BridgeSendFees

sealed interface SendFee {

    data class Bridge(
        val fee: BridgeSendFees,
        val updateTimeMs: Long = System.currentTimeMillis()
    ) : SendFee
}

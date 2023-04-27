package org.p2p.wallet.bridge.send.statemachine.model

import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits

sealed interface SendFee {

    data class Bridge(
        val fee: BridgeSendFees,
        val feeLimitInfo: TransactionFeeLimits?,
        val updateTimeMs: Long = System.currentTimeMillis()
    ) : SendFee
}

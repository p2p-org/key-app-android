package org.p2p.wallet.bridge.send.model

import org.p2p.wallet.bridge.model.BridgeFee

data class BridgeSendFees(
    val networkFee: BridgeFee,
    val messageAccountRent: BridgeFee,
    val bridgeFee: BridgeFee,
    val arbiterFee: BridgeFee
)

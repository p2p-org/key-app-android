package org.p2p.wallet.bridge.send.model

import org.p2p.wallet.bridge.model.BridgeFee

data class BridgeSendFees(
    val networkFee: BridgeFee,
    val networkFeeInToken: BridgeFee,
    val messageAccountRent: BridgeFee,
    val messageAccountRentInToken: BridgeFee,
    val bridgeFee: BridgeFee,
    val bridgeFeeInToken: BridgeFee,
    val arbiterFee: BridgeFee,
    val resultAmount: BridgeFee,
)

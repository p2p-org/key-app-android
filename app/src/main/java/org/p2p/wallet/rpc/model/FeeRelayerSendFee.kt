package org.p2p.wallet.rpc.model

import java.math.BigInteger

data class FeeRelayerSendFee(
    val feeInSol: BigInteger,
    val feeInPayingToken: BigInteger?
)

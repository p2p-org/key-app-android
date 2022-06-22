package org.p2p.wallet.feerelayer.model

import java.math.BigInteger

data class FeeRelayerFee(
    val feeInSol: BigInteger,
    val feeInPayingToken: BigInteger?
)

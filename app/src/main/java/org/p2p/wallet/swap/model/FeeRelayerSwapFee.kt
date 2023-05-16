package org.p2p.wallet.swap.model

import java.math.BigInteger

@Deprecated("Old swap")
class FeeRelayerSwapFee(
    val feeInSol: BigInteger,
    val feeInPayingToken: BigInteger,
    val isFreeTransactionAvailable: Boolean
)

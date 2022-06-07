package org.p2p.wallet.swap.model

import java.math.BigInteger

class FeeRelayerSwapFee(
    val feeInSol: BigInteger,
    val feeInPayingToken: BigInteger,
    val transactionFeeInSol: BigInteger,
    val transactionFeeInPayingToken: BigInteger,
    val isFreeTransactionAvailable: Boolean
)

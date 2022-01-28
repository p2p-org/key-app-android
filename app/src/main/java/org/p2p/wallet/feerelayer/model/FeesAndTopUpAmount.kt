package org.p2p.wallet.feerelayer.model

import java.math.BigInteger

class FeesAndTopUpAmount(
    val feeInSOL: FeeAmount?,
    val topUpAmountInSOL: BigInteger?,
    val feeInPayingToken: FeeAmount?,
    val topUpAmountInPayingToen: BigInteger?
)
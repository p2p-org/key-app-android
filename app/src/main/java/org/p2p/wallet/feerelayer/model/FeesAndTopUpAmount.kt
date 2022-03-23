package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.core.FeeAmount
import java.math.BigInteger

class FeesAndTopUpAmount(
    val feeInSOL: FeeAmount,
    val topUpAmountInSOL: BigInteger?,
    val feeInPayingToken: FeeAmount?,
    val topUpAmountInPayingToken: BigInteger?
)

package org.p2p.wallet.feerelayer.model

import java.math.BigInteger

class TopUpAndActionPreparedParams(
    val topUpFeesAndPools: FeesAndPools?,
    val actionFeesAndPools: FeesAndPools,
    val topUpAmount: BigInteger?
)